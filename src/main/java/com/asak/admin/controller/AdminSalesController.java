package com.asak.admin.controller;

import com.asak.admin.dto.response.dashboard.AdminDashboardResponse;
import com.asak.admin.dto.response.sales.SalesSummaryResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesResponse;
import com.asak.admin.dto.response.sales.daily.DailySalesTimeSlotResponse;
import com.asak.admin.dto.response.sales.month.MonthlySalesResponse;
import com.asak.admin.service.AdminSalesService;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO-015~018: 매출 API는 화면별 DTO가 아니라 API 책임별 DTO를 반환한다.
 *
 * <p>순서: dashboard → summary → monthly → daily → daily/time-slots. 시간대 버킷은 daily 응답에 섞지 않고 별도
 * endpoint에서 조회한다.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminSalesController {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

  private final AdminSalesService adminSalesService;

  public AdminSalesController(AdminSalesService adminSalesService) {
    this.adminSalesService = adminSalesService;
  }

  /** Dashboard: 기존 매출·주문·품절 View를 한 화면용 응답으로 조립한다. */
  @GetMapping("/dashboard")
  public ApiResponse<AdminDashboardResponse> getDashboard() {
    return ApiResponse.success(
        "ADMIN_DASHBOARD_SUCCESS", "대시보드 요약", adminSalesService.getDashboard());
  }

  /**
   * Sales Summary: today, week, month 중 하나의 기간 요약을 반환한다. period가 없으면 startDate/endDate 범위를, 그마저 없으면
   * 이번 달 1일~오늘을 사용한다(Service 기본값).
   */
  @GetMapping("/sales/summary")
  public ApiResponse<SalesSummaryResponse> getSalesSummary(
      @RequestParam(required = false) String period,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    if (period != null && !AdminSalesService.isSupportedPeriod(period)) {
      return ApiResponse.error(ErrorCode.SALES_PERIOD_INVALID);
    }

    LocalDate parsedStartDate = null;
    LocalDate parsedEndDate = null;
    if (period == null && startDate != null && !startDate.isBlank()) {
      try {
        parsedStartDate = LocalDate.parse(startDate);
        parsedEndDate =
            (endDate == null || endDate.isBlank()) ? parsedStartDate : LocalDate.parse(endDate);
      } catch (DateTimeException exception) {
        return ApiResponse.error(ErrorCode.SALES_DATE_INVALID);
      }

      LocalDate today = LocalDate.now(KOREA_ZONE_ID);
      if (parsedStartDate.isAfter(today)
          || parsedEndDate.isAfter(today)
          || parsedEndDate.isBefore(parsedStartDate)) {
        return ApiResponse.error(ErrorCode.DATE_RANGE_INVALID);
      }
    }

    return ApiResponse.success(
        "ADMIN_SALES_SUMMARY_SUCCESS",
        "매출 요약",
        adminSalesService.getSalesSummary(period, parsedStartDate, parsedEndDate));
  }

  /** Monthly: 선택 연도의 월별 행과 월별 인기 메뉴를 반환한다. */
  @GetMapping("/sales/monthly")
  public ApiResponse<MonthlySalesResponse> getMonthlySales(@RequestParam int year) {
    int currentYear = LocalDate.now(KOREA_ZONE_ID).getYear();
    if (year < adminSalesService.getMinYear() || year > currentYear) {
      return ApiResponse.error(ErrorCode.SALES_YEAR_INVALID);
    }
    return ApiResponse.success(
        "ADMIN_SALES_MONTHLY_SUCCESS", "월별 매출", adminSalesService.getMonthlySales(year));
  }

  /** Daily: from~to 일자 행, 선택 종료일의 분해·랭킹 데이터를 반환한다. */
  @GetMapping("/sales/daily")
  public ApiResponse<DailySalesResponse> getDailySales(
      @RequestParam(required = false) String from, @RequestParam(required = false) String to) {

    LocalDate today = LocalDate.now(KOREA_ZONE_ID);

    LocalDate startDate;
    LocalDate endDate;

    try {
      if (from == null || from.isBlank()) {
        // 파라미터 없으면 이번 달 1일 ~ 오늘
        startDate = today.withDayOfMonth(1);
        endDate = today;
      } else {
        startDate = LocalDate.parse(from);
        endDate = (to == null || to.isBlank()) ? startDate : LocalDate.parse(to);
      }
    } catch (DateTimeException exception) {
      return ApiResponse.error(ErrorCode.SALES_DATE_INVALID);
    }

    if (startDate.isAfter(today) || endDate.isAfter(today) || endDate.isBefore(startDate)) {
      return ApiResponse.error(ErrorCode.DATE_RANGE_INVALID);
    }

    return ApiResponse.success(
        "ADMIN_SALES_DAILY_SUCCESS", "일별 매출", adminSalesService.getDailySales(startDate, endDate));
  }

  /** Daily Time Slots: 30분 또는 60분 버킷은 영업시간(10:00~22:00) 안에서만 반환한다. */
  @GetMapping("/sales/daily/time-slots")
  public ApiResponse<List<DailySalesTimeSlotResponse>> getDailyTimeSlots(
      @RequestParam String date, @RequestParam(defaultValue = "60") int intervalMinutes) {
    if (intervalMinutes != 30 && intervalMinutes != 60) {
      return ApiResponse.error(ErrorCode.SALES_INTERVAL_INVALID);
    }

    LocalDate salesDate;
    try {
      salesDate = LocalDate.parse(date);
    } catch (DateTimeException exception) {
      return ApiResponse.error(ErrorCode.SALES_DATE_INVALID);
    }
    if (salesDate.isAfter(LocalDate.now(KOREA_ZONE_ID))) {
      return ApiResponse.error(ErrorCode.DATE_RANGE_INVALID);
    }

    return ApiResponse.success(
        "ADMIN_SALES_TIME_SLOTS_SUCCESS",
        "시간대별 매출",
        adminSalesService.getDailySalesTimeSlots(salesDate, intervalMinutes));
  }
}
