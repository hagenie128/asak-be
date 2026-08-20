package com.asak.admin.service;

import com.asak.admin.dto.response.dashboard.AdminDashboardResponse;
import com.asak.admin.dto.response.dashboard.DashboardInventoryAlertResponse;
import com.asak.admin.dto.response.dashboard.DashboardOrderTypeSummaryResponse;
import com.asak.admin.dto.response.sales.HourlySalesResponse;
import com.asak.admin.dto.response.sales.MenuSalesRankingResponse;
import com.asak.admin.dto.response.sales.SalesKpiResponse;
import com.asak.admin.dto.response.sales.SalesSummaryResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesBreakdownResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesRowResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesTimeSlotResponse;
import com.asak.admin.dto.response.sales.month.MonthlySalesResponse;
import com.asak.admin.dto.response.sales.month.MonthlySalesRowResponse;
import com.asak.admin.mapper.AdminSalesMapper;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * TODO-018: View의 업무 데이터를 화면 DTO로 조립한다.
 *
 * <p>DB는 실제 매출만 반환한다. 일자·시간대의 빈 구간 0-fill과 표시용 label은 Service 책임이며, chart의 높이·fill 같은 렌더링 값은 반환하지
 * 않는다.
 */
@Service
public class AdminSalesService {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final LocalTime BUSINESS_OPEN_TIME = LocalTime.of(10, 0);
  private static final LocalTime BUSINESS_CLOSE_TIME = LocalTime.of(22, 0);

  private final AdminSalesMapper adminSalesMapper;

  public AdminSalesService(AdminSalesMapper adminSalesMapper) {
    this.adminSalesMapper = adminSalesMapper;
  }

  public static boolean isSupportedPeriod(String period) {
    return "today".equals(period) || "week".equals(period) || "month".equals(period);
  }

  /** Dashboard는 기존 매출·주문·품절 조회를 하나의 화면 응답으로 조립한다. */
  public AdminDashboardResponse getDashboard() {
    LocalDate today = LocalDate.now(KOREA_ZONE_ID);
    LocalDate yesterday = today.minusDays(1);
    Map<String, Object> kpi = adminSalesMapper.getDashboardKpi(today, yesterday);
    Map<String, Object> orderType = adminSalesMapper.getDashboardOrderTypeSummary(today);
    Map<String, Object> inventory = adminSalesMapper.getDashboardInventorySummary();
    var recentOrders = adminSalesMapper.getDashboardRecentOrders();
    var statusSummary = adminSalesMapper.getDashboardStatusSummary(today);
    var weeklySales = adminSalesMapper.getDashboardWeeklySales(dateRange(today.minusDays(6), today));

    return AdminDashboardResponse.builder()
        .dateLabel(today.toString())
        .kpis(
            List.of(
                buildKpiResponse(
                    "오늘 매출",
                    longValue(kpi, "netSalesAmount"),
                    longValue(kpi, "prevNetSalesAmount"),
                    "today"),
                buildKpiResponse(
                    "주문 수",
                    longValue(kpi, "orderCount"),
                    longValue(kpi, "prevOrderCount"),
                    "today"),
                buildKpiResponse(
                    "평균 객단가",
                    longValue(kpi, "averageOrderAmount"),
                    longValue(kpi, "prevAverageOrderAmount"),
                    "today"),
                // 진행 중 주문은 순간값이라 "전일" 개념이 없다. 기존 로직이 label의 "주문" 일치로
                // 전일 주문 수(prevOrderCount)를 델타 기준으로 재사용하던 동작을 그대로 유지한다.
                buildKpiResponse(
                    "진행 중 주문",
                    longValue(kpi, "activeOrderCount"),
                    longValue(kpi, "prevOrderCount"),
                    "today")))
        .recentOrders(recentOrders)
        .statusSummary(statusSummary)
        .orderTypeSummary(
            DashboardOrderTypeSummaryResponse.builder()
                .eatIn((int) longValue(orderType, "eatIn"))
                .takeOut((int) longValue(orderType, "takeOut"))
                .build())
        .inventoryAlerts(
            List.of(
                inventoryAlert("메뉴 품절", longValue(inventory, "menuSoldOut")),
                inventoryAlert("재료 품절", longValue(inventory, "ingredientSoldOut")),
                inventoryAlert("옵션 품절", longValue(inventory, "optionSoldOut"))))
        .weeklySales(weeklySales)
        .build();
  }

  /** Summary는 기간 요약, 시간대 매출, 비중, 랭킹을 한 응답으로 반환한다. */
  public SalesSummaryResponse getSalesSummary(
      @Nullable String period, @Nullable LocalDate startDate, @Nullable LocalDate endDate) {

    LocalDate today = LocalDate.now(KOREA_ZONE_ID);

    Map<String, Object> range;

    if (period != null) {
      range = periodRange(period, today);
    } else if (startDate != null && endDate != null) {
      range = dateRange(startDate, endDate);
    } else {
      range = dateRange(today.withDayOfMonth(1), today);
    }

    LocalDate rangeStartDate = (LocalDate) range.get("startDate");
    LocalDate rangeEndDate = (LocalDate) range.get("endDate");

    boolean isSingleDay = rangeStartDate.equals(rangeEndDate);

    List<HourlySalesResponse> hourlySales = List.of();
    List<DailySalesRowResponse> dailySales = List.of();

    long netSales;
    long orderCount;

    if (isSingleDay) {
      // 하루 조회 → 시간대별 매출
      hourlySales = adminSalesMapper.getHourlySalesByRange(range);

      netSales = hourlySales.stream().mapToLong(HourlySalesResponse::getSalesAmount).sum();

      orderCount = hourlySales.stream().mapToLong(HourlySalesResponse::getOrderCount).sum();

    } else {
      // 기간 조회 → 일별 매출
      dailySales = adminSalesMapper.getDailySalesRows(range);

      netSales = dailySales.stream().mapToLong(DailySalesRowResponse::getTotalAmount).sum();

      orderCount = dailySales.stream().mapToLong(DailySalesRowResponse::getOrderCount).sum();
    }

    return SalesSummaryResponse.builder()
        .label(periodLabel(period))
        .dateRange(rangeStartDate + " ~ " + rangeEndDate)
        .availablePeriods(List.of("today", "week", "month"))
        .kpis(
            List.of(
                kpi("총매출", netSales, range, period),
                kpi("주문 수", orderCount, range, period),
                kpi("평균 객단가", average(netSales, orderCount), range, period)))
        .hourlySales(hourlySales)
        .dailySales(dailySales)
        .paymentShare(adminSalesMapper.getPaymentShare(range))
        .orderShare(adminSalesMapper.getOrderShare(range))
        .ranking(adminSalesMapper.getMenuRankingByRange(range))
        .build();
  }

  /** Monthly는 연도별 월 행과 해당 월의 랭킹을 반환한다. */
  public MonthlySalesResponse getMonthlySales(int year) {
    List<MonthlySalesRowResponse> rows = adminSalesMapper.getMonthlySalesRows(year);
    Map<String, List<MenuSalesRankingResponse>> ranking = new HashMap<>();
    LocalDate today = LocalDate.now(KOREA_ZONE_ID);

    for (MonthlySalesRowResponse row : rows) {
      YearMonth month = YearMonth.parse(row.getMonth());
      LocalDate endDate = month.equals(YearMonth.from(today)) ? today : month.atEndOfMonth();
      ranking.put(
          row.getMonth(),
          adminSalesMapper.getMenuRankingByRange(dateRange(month.atDay(1), endDate)));
    }

    return MonthlySalesResponse.builder().year(year).rows(rows).ranking(ranking).build();
  }

  /** Daily는 범위의 일별 행과 선택 종료일의 세부 비중·랭킹을 반환한다. */
  public DailySalesResponse getDailySales(LocalDate from, LocalDate to) {
    Map<String, Object> range = dateRange(from, to);
    Map<String, List<MenuSalesRankingResponse>> ranking =
        Map.of(to.toString(), adminSalesMapper.getMenuRankingByRange(dateRange(to, to)));
    Map<String, DailySalesBreakdownResponse> breakdown =
        Map.of(
            to.toString(),
            DailySalesBreakdownResponse.builder()
                .paymentShare(adminSalesMapper.getPaymentShare(dateRange(to, to)))
                .orderShare(adminSalesMapper.getOrderShare(dateRange(to, to)))
                .build());

    return DailySalesResponse.builder()
        .from(from)
        .to(to)
        .rows(adminSalesMapper.getDailySalesRows(range))
        .ranking(ranking)
        .breakdown(breakdown)
        .build();
  }

  /** Time slots는 실제 행을 가져온 뒤 영업시간 내 빠진 구간만 0으로 채운다. */
  public List<DailySalesTimeSlotResponse> getDailySalesTimeSlots(
      LocalDate salesDate, int intervalMinutes) {
    Map<String, DailySalesTimeSlotResponse> slotsByTime = new HashMap<>();
    for (DailySalesTimeSlotResponse slot :
        adminSalesMapper.getDailySalesTimeSlots(
            Map.of("salesDate", salesDate, "intervalMinutes", intervalMinutes))) {
      slotsByTime.put(slotKey(slot.getSalesHour(), slot.getSalesMinute()), slot);
    }

    LocalTime lastSlotStart = BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes);
    LocalDate today = LocalDate.now(KOREA_ZONE_ID);
    if (salesDate.equals(today)) {
      LocalTime now = LocalTime.now(KOREA_ZONE_ID);
      if (now.isBefore(BUSINESS_OPEN_TIME)) {
        return List.of();
      }
      lastSlotStart =
          LocalTime.of(now.getHour(), now.getMinute() / intervalMinutes * intervalMinutes);
      if (lastSlotStart.isAfter(BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes))) {
        lastSlotStart = BUSINESS_CLOSE_TIME.minusMinutes(intervalMinutes);
      }
    }

    List<DailySalesTimeSlotResponse> result = new ArrayList<>();
    for (LocalTime time = BUSINESS_OPEN_TIME;
        !time.isAfter(lastSlotStart);
        time = time.plusMinutes(intervalMinutes)) {
      result.add(
          slotsByTime.getOrDefault(
              slotKey(time.getHour(), time.getMinute()),
              DailySalesTimeSlotResponse.builder()
                  .salesHour(time.getHour())
                  .salesMinute(time.getMinute())
                  .orderCount(0)
                  .netSalesAmount(0)
                  .averageOrderAmount(0)
                  .build()));
    }
    return result;
  }

  public int getMinYear() {
    int minYear = adminSalesMapper.getMinYear();
    return minYear == 0 ? LocalDate.now(KOREA_ZONE_ID).getYear() : minYear;
  }

  private Map<String, Object> periodRange(@Nullable String period, LocalDate today) {
    return switch (period) {
      case "today" -> dateRange(today, today);
      case "week" -> dateRange(today.minusDays(6), today);
      default -> dateRange(today.withDayOfMonth(1), today);
    };
  }

  private Map<String, Object> dateRange(LocalDate startDate, LocalDate endDate) {
    Map<String, Object> range = new HashMap<>();
    range.put("startDate", startDate);
    range.put("endDate", endDate);
    return range;
  }

  private SalesKpiResponse kpi(
      String label, long value, @Nullable Map<String, Object> range, String period) {

    LocalDate startDate;
    LocalDate endDate;
    if (range != null) {
      startDate = (LocalDate) range.get("startDate");
      endDate = (LocalDate) range.get("endDate");
    } else {
      startDate = LocalDate.now(KOREA_ZONE_ID);
      endDate = LocalDate.now(KOREA_ZONE_ID).minusDays(1);
    }
    long size = endDate.toEpochDay() - startDate.toEpochDay();
    Map<String, Object> before = new HashMap<>();
    if (size == 0) {
      before.put("startDate", startDate.minusDays(1));
      before.put("endDate", endDate.minusDays(1));
    } else {
      before.put("startDate", startDate.minusDays(size + 1));
      before.put("endDate", startDate.minusDays(1));
    }
    long beforeValue;
    if (label.contains("매출")) {
      beforeValue = adminSalesMapper.getDailySales(before);
    } else if (label.contains("주문")) {
      beforeValue = adminSalesMapper.getDailyOrderCount(before);
    } else if (label.contains("객단가")) {
      beforeValue =
          adminSalesMapper.getDailySales(before) / adminSalesMapper.getDailyOrderCount(before);
    } else {
      beforeValue = 0;
    }

    return buildKpiResponse(label, value, beforeValue, period);
  }

  /** delta·표시 문자열 조립만 담당한다. beforeValue 조회는 호출부 책임이다(추가 DB 호출을 피하기 위함). */
  private SalesKpiResponse buildKpiResponse(
      String label, long value, long beforeValue, String period) {
    double delta;

    if (beforeValue == 0) {
      delta = value == 0 ? 0.0 : 100.0;
    } else {
      delta = Math.round(((double) value - beforeValue) / beforeValue * 100);
    }

    String deltaLabel = "";
    switch (period) {
      case "today" -> deltaLabel = "전일 대비";
      case "week" -> deltaLabel = "전주 대비";
      case "month" -> deltaLabel = "전월 대비";
      default -> deltaLabel = "기간 대비";
    }
    return SalesKpiResponse.builder()
        .label(label)
        .value(value)
        .display(
            String.format(
                Locale.KOREA,
                "%,d%s",
                value,
                label.contains("매출") || label.contains("객단가") ? "원" : "건"))
        // ↑ 1.1% 전일 대비
        .delta(delta)
        .deltaLabel(deltaLabel)
        .build();
  }

  private DashboardInventoryAlertResponse inventoryAlert(String label, long count) {
    return DashboardInventoryAlertResponse.builder()
        .label(label)
        .badge(count + "건")
        .tone(count > 0 ? "warning" : "normal")
        .build();
  }

  private long longValue(Map<String, Object> values, String key) {
    Object value = values.get(key);
    return value instanceof Number number ? new BigDecimal(number.toString()).longValue() : 0L;
  }

  private long average(long amount, long count) {
    return count == 0 ? 0 : Math.round((double) amount / count);
  }

  private String periodLabel(String period) {
    return switch (period) {
      case "today" -> "오늘";
      case "week" -> "이번 주";
      case "month" -> "이번 달";
      default -> "기간";
    };
  }

  private String slotKey(int hour, int minute) {
    return hour + ":" + minute;
  }
}
