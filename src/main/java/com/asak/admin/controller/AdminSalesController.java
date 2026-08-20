package com.asak.admin.controller;

import com.asak.admin.dto.response.dashboard.AdminDashboardResponse;
import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.HourlySalesSummaryItemResponse;
import com.asak.admin.service.AdminSalesService;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminSalesController {

  private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
  private final AdminSalesService adminSalesService;

  public AdminSalesController(AdminSalesService adminSalesService) {
    this.adminSalesService = adminSalesService;
  }

  @GetMapping("/sales/summary")
  public ApiResponse<List<DailySalesSummaryItemResponse>> getSalesSummary(
      @RequestParam String startDate, @RequestParam @Nullable String endDate) {
    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = endDate == null || endDate.isEmpty() ? start : LocalDate.parse(endDate);
    LocalDate today = LocalDate.now(KOREA_ZONE_ID);
    if (start.isAfter(today)) return ApiResponse.error(ErrorCode.START_DATE_GREATER_THAN_TODAY);
    if (end.isAfter(today)) return ApiResponse.error(ErrorCode.END_DATE_GREATER_THAN_TODAY);
    if (end.isBefore(start)) return ApiResponse.error(ErrorCode.END_DATE_LESS_THAN_START_DATE);
    return ApiResponse.success("ADMIN_SALES_SUMMARY_SUCCESS", "관리자 매출 요약 조회 성공", adminSalesService.getSalesSummary(start, end));
  }

  @GetMapping("/sales/monthly")
  public ApiResponse<List<DailySalesSummaryItemResponse>> getMonthlySalesSummary(
      @RequestParam int year, @RequestParam int month) {
    if (year < adminSalesService.getMinYear()) return ApiResponse.error(ErrorCode.YEAR_LESS_THAN_MIN_YEAR);
    if (year > LocalDate.now(KOREA_ZONE_ID).getYear()) return ApiResponse.error(ErrorCode.YEAR_GREATER_THAN_CURRENT_YEAR);
    if (month < 1 || month > 12) return ApiResponse.error(ErrorCode.DATE_RANGE_INVALID);
    return ApiResponse.success("ADMIN_MONTHLY_SALES_SUCCESS", "관리자 월별 일자별 매출 조회 성공", adminSalesService.getMonthlySalesSummary(year, month));
  }

  @GetMapping("/sales/daily")
  public ApiResponse<List<HourlySalesSummaryItemResponse>> getDailySales(@RequestParam String date, @RequestParam(defaultValue = "60") int intervalMinutes) {
    return timeSlots(date, intervalMinutes);
  }

  @GetMapping("/sales/hourly")
  public ApiResponse<List<HourlySalesSummaryItemResponse>> getHourlySales(@RequestParam String date, @RequestParam(defaultValue = "60") int intervalMinutes) {
    return timeSlots(date, intervalMinutes);
  }

  @GetMapping("/dashboard")
  public ApiResponse<AdminDashboardResponse> getDashboard() {
    return ApiResponse.success("ADMIN_DASHBOARD_SUCCESS", "관리자 대시보드 조회 성공", adminSalesService.getDashboard());
  }

  private ApiResponse<List<HourlySalesSummaryItemResponse>> timeSlots(String date, int intervalMinutes) {
    if (intervalMinutes != 30 && intervalMinutes != 60) return ApiResponse.error(ErrorCode.SALES_INTERVAL_INVALID);
    LocalDate salesDate = LocalDate.parse(date);
    if (salesDate.isAfter(LocalDate.now(KOREA_ZONE_ID))) return ApiResponse.error(ErrorCode.START_DATE_GREATER_THAN_TODAY);
    return ApiResponse.success("ADMIN_SALES_HOURLY_SUCCESS", "관리자 시간별 매출 조회 성공", adminSalesService.getDailySalesTimeSlots(salesDate, intervalMinutes));
  }
}
