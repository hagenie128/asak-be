package com.asak.admin.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.dashboard.AdminDashboardResponse;
import com.asak.admin.dto.response.sales.DailyTopMenuResponse;
import com.asak.admin.dto.response.sales.HourlySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.HourlyTopMenuResponse;
import com.asak.admin.mapper.AdminSalesMapper;

// TODO-018: summary/monthly/daily/dashboard 집계 로직을 Mapper 호출과 DTO 조립으로 구현한다.
// Controller의 날짜 query를 검증된 값으로 받고, 집계 기준(주문 상태·시간대·0값)을 네 endpoint에서 일관되게 적용한다.
// view를 사용하면 실제 DB 정의와 성능을 확인하고, frontend TODO-022/025 연결 전 빈 기간 응답을 API로 검증한다.
@Service
public class AdminSalesService {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final LocalTime BUSINESS_OPEN_TIME = LocalTime.of(10, 0);
  private static final LocalTime BUSINESS_CLOSE_TIME = LocalTime.of(22, 0);

  private final AdminSalesMapper adminSalesMapper;

  public AdminSalesService(AdminSalesMapper adminSalesMapper) {
    this.adminSalesMapper = adminSalesMapper;
  }

  public AdminDashboardResponse getDashboard() {
    LocalDate today = LocalDate.now(KOREA_ZONE_ID);
    Map<String, Object> kpi = adminSalesMapper.getDashboardKpi(today);
    Map<String, Object> orderType = adminSalesMapper.getDashboardOrderTypeSummary(today);
    Map<String, Object> inventory = adminSalesMapper.getDashboardInventorySummary();
    Map<String, Object> dateRange = new HashMap<>();
    dateRange.put("startDate", today.minusDays(6));
    dateRange.put("endDate", today);

    return AdminDashboardResponse.builder()
        .date(today.toString())
        .kpis(List.of(
            dashboardKpi("순매출", decimal(kpi, "netSalesAmount")),
            dashboardKpi("주문 수", decimal(kpi, "orderCount")),
            dashboardKpi("평균 객단가", decimal(kpi, "averageOrderAmount")),
            dashboardKpi("진행 중 주문", decimal(kpi, "activeOrderCount"))))
        .recentOrders(adminSalesMapper.getDashboardRecentOrders())
        .statusSummary(adminSalesMapper.getDashboardStatusSummary(today))
        .orderTypeSummary(AdminDashboardResponse.OrderTypeSummary.builder()
            .eatIn(decimal(orderType, "eatIn").longValue())
            .takeOut(decimal(orderType, "takeOut").longValue())
            .build())
        .inventoryAlerts(List.of(
            inventoryAlert("메뉴 품절", decimal(inventory, "menuSoldOut")),
            inventoryAlert("재료 품절", decimal(inventory, "ingredientSoldOut")),
            inventoryAlert("옵션 품절", decimal(inventory, "optionSoldOut"))))
        .weeklySales(adminSalesMapper.getDashboardWeeklySales(dateRange).stream()
            .map(item -> AdminDashboardResponse.WeeklySales.builder()
                .label(item.getSalesDate().toString())
                .amount(item.getNetSalesAmount())
                .build())
            .toList())
        .build();
  }

  private AdminDashboardResponse.Kpi dashboardKpi(String label, BigDecimal value) {
    return AdminDashboardResponse.Kpi.builder().label(label).value(value).build();
  }

  private AdminDashboardResponse.InventoryAlert inventoryAlert(String label, BigDecimal count) {
    return AdminDashboardResponse.InventoryAlert.builder()
        .label(label)
        .badge(count.toPlainString() + "건")
        .tone(count.signum() > 0 ? "warning" : "normal")
        .build();
  }

  private BigDecimal decimal(Map<String, Object> values, String key) {
    Object value = values.get(key);
    return value instanceof Number number ? new BigDecimal(number.toString()) : BigDecimal.ZERO;
  }

  public List<DailySalesSummaryItemResponse> getSalesSummary(
      LocalDate startDate, LocalDate endDate) {

    Map<String, Object> dateRange = new HashMap<>();
    dateRange.put("startDate", startDate);
    dateRange.put("endDate", endDate);
    List<DailySalesSummaryItemResponse> response = adminSalesMapper.getSalesSummary(dateRange);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<HourlySalesSummaryItemResponse> getSalesHourly(LocalDate date) {
    List<HourlySalesSummaryItemResponse> response = adminSalesMapper.getSalesHourly(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<HourlySalesSummaryItemResponse> getDailySalesTimeSlots(
      LocalDate salesDate, int intervalMinutes) {
    Map<String, Object> params = new HashMap<>();
    params.put("salesDate", salesDate);
    params.put("intervalMinutes", intervalMinutes);

    Map<String, HourlySalesSummaryItemResponse> slotsByTime = new HashMap<>();
    for (HourlySalesSummaryItemResponse item : adminSalesMapper.getDailySalesTimeSlots(params)) {
      slotsByTime.put(toSlotKey(item.getSalesHour(), item.getSalesMinute()), item);
    }

    LocalDate today = LocalDate.now(KOREA_ZONE_ID);
    LocalTime lastSlotStart = BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes);
    if (salesDate.equals(today)) {
      LocalTime now = LocalTime.now(KOREA_ZONE_ID);
      if (now.isBefore(BUSINESS_OPEN_TIME)) {
        return Collections.emptyList();
      }
      int minute = now.getMinute() / intervalMinutes * intervalMinutes;
      lastSlotStart = LocalTime.of(now.getHour(), minute);
      if (lastSlotStart.isAfter(BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes))) {
        lastSlotStart = BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes);
      }
    }

    List<HourlySalesSummaryItemResponse> result = new ArrayList<>();
    for (LocalTime slotStart = BUSINESS_OPEN_TIME;
        !slotStart.isAfter(lastSlotStart);
        slotStart = slotStart.plusMinutes(intervalMinutes)) {
      result.add(
          slotsByTime.getOrDefault(
              toSlotKey(slotStart.getHour(), slotStart.getMinute()),
              emptyTimeSlot(salesDate, slotStart)));
    }
    return result;
  }

  private String toSlotKey(Integer hour, Integer minute) {
    return hour + ":" + minute;
  }

  private HourlySalesSummaryItemResponse emptyTimeSlot(LocalDate salesDate, LocalTime slotStart) {
    return HourlySalesSummaryItemResponse.builder()
        .salesDate(Date.valueOf(salesDate))
        .salesHour(slotStart.getHour())
        .salesMinute(slotStart.getMinute())
        .orderCount(BigInteger.ZERO)
        .canceledOrderCount(BigInteger.ZERO)
        .grossSalesAmount(BigDecimal.ZERO)
        .canceledAmount(BigDecimal.ZERO)
        .netSalesAmount(BigDecimal.ZERO)
        .averageOrderAmount(BigDecimal.ZERO)
        .cancelRate(BigDecimal.ZERO)
        .build();
  }

  public List<DailyTopMenuResponse> getDailyTopMenu(LocalDate date) {
    List<DailyTopMenuResponse> response = adminSalesMapper.getDailyTopMenu(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public List<HourlyTopMenuResponse> getHourlyTopMenu(LocalDate date) {
    List<HourlyTopMenuResponse> response = adminSalesMapper.getHourlyTopMenu(date);
    if (response.isEmpty()) {
      return Collections.emptyList();
    }
    return response;
  }

  public int getMinYear() {
    return adminSalesMapper.getMinYear();
  }

  public List<DailySalesSummaryItemResponse> getMonthlySalesSummary(int year, int month) {
    YearMonth targetMonth = YearMonth.of(year, month);
    YearMonth currentMonth = YearMonth.now(KOREA_ZONE_ID);
    if (targetMonth.isAfter(currentMonth)) return Collections.emptyList();
    LocalDate endDate = targetMonth.equals(currentMonth) ? LocalDate.now(KOREA_ZONE_ID) : targetMonth.atEndOfMonth();
    Map<String, Object> dateRange = new HashMap<>();
    dateRange.put("startDate", targetMonth.atDay(1));
    dateRange.put("endDate", endDate);
    Map<LocalDate, DailySalesSummaryItemResponse> byDate = new HashMap<>();
    for (DailySalesSummaryItemResponse item : adminSalesMapper.getMonthlyDailySales(dateRange)) {
      byDate.put(new java.sql.Date(item.getSalesDate().getTime()).toLocalDate(), item);
    }
    List<DailySalesSummaryItemResponse> result = new ArrayList<>();
    for (LocalDate date = targetMonth.atDay(1); !date.isAfter(endDate); date = date.plusDays(1)) {
      DailySalesSummaryItemResponse item = byDate.get(date);
      if (item == null) {
        item = new DailySalesSummaryItemResponse(Date.valueOf(date), BigInteger.ZERO, BigInteger.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
      }
      result.add(item);
    }
    return result;
  }
}
