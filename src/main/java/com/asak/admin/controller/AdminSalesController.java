package com.asak.admin.controller;

import com.asak.admin.dto.response.sales.DailySalesSummaryItemResponse;
import com.asak.admin.dto.response.sales.MonthlySalesSummaryItemResponse;
import com.asak.admin.service.AdminSalesService;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminSalesController {

  private final AdminSalesService adminSalesService;

  public AdminSalesController(AdminSalesService adminSalesService) {
    this.adminSalesService = adminSalesService;
  }

  // TODO-015: GET /api/admin/sales/summary?startDate&endDate. 날짜 범위 validation·빈
  // 기간 200 응답을 먼저 정한다.
  @GetMapping("/sales/summary")
  public ApiResponse<List<DailySalesSummaryItemResponse>> getSalesSummary(
      @RequestParam String startDate, @RequestParam @Nullable String endDate) {

    if (startDate == null || startDate.isEmpty()) {
      return ApiResponse.error(ErrorCode.START_DATE_REQUIRED);
    }
    LocalDate startDateLocal = LocalDate.parse(startDate);
    LocalDate endDateLocal =
        endDate == null || endDate.isEmpty() ? startDateLocal : LocalDate.parse(endDate);
    LocalDate today = LocalDate.now();

    if (startDateLocal.isAfter(today)) {
      return ApiResponse.error(ErrorCode.START_DATE_GREATER_THAN_TODAY);
    }
    if (endDateLocal.isAfter(today)) {
      return ApiResponse.error(ErrorCode.END_DATE_GREATER_THAN_TODAY);
    }
    if (endDateLocal.isBefore(startDateLocal)) {
      return ApiResponse.error(ErrorCode.END_DATE_LESS_THAN_START_DATE);
    }

    List<DailySalesSummaryItemResponse> responses =
        adminSalesService.getSalesSummary(startDateLocal, endDateLocal);
    if (responses.isEmpty()) {
      return ApiResponse.error(ErrorCode.SALES_SUMMARY_NOT_FOUND);
    }
    return ApiResponse.success("ADMIN_SALES_SUMMARY_SUCCESS", "관리자 매출 요약 조회 성공", responses);
  }

  // TODO-016: GET /api/admin/sales/monthly?year. year 범위와 월이 없는 경우의 0값/누락 표현을
  // DTO로 고정한다.
  @GetMapping("/sales/monthly")
  public ApiResponse<List<MonthlySalesSummaryItemResponse>> getMonthlySalesSummary(
      @RequestParam int year) {
    return ApiResponse.success(
        "ADMIN_MONTHLY_SALES_SUCCESS", "관리자 월별 매출 조회 성공", Collections.emptyList());
  }
  // TODO-017: GET /api/admin/sales/daily?date. 매장 시간대 기준 일자와 주문 상태 포함 기준을 명시한다.
  // TODO-023: GET /api/admin/dashboard. summary와 중복 집계를 피하고 TODO-024/025이 소비할 단일
  // 응답 DTO를 확정한다.
  // 구현 순서: TODO-018 Mapper/XML → Service → 이 Controller → frontend TODO-019~025;
  // 각 endpoint는
  // query·빈 데이터·오류를 검증한다.
}
