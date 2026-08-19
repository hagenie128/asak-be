package com.asak.admin.controller;

import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.dto.response.sales.SalesSummaryResponse;
import com.asak.admin.service.AdminSalesService;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;

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
  public ApiResponse<SalesSummaryResponse> getSalesSummary(@RequestParam String startDate,
      @RequestParam @Nullable String endDate) {

        
    if (startDate == null) {
      return ApiResponse.error(ErrorCode.DATE_RANGE_INVALID);
    }

    SalesSummaryResponse response = adminSalesService.getSalesSummary(startDate, endDate);
    if (response == null) {
      return ApiResponse.error(ErrorCode.SALES_SUMMARY_NOT_FOUND);
    }

    return ApiResponse.success("ADMIN_SALES_SUMMARY_SUCCESS", "관리자 매출 요약 조회 성공", response);
  }
  // TODO-016: GET /api/admin/sales/monthly?year. year 범위와 월이 없는 경우의 0값/누락 표현을
  // DTO로 고정한다.
  // TODO-017: GET /api/admin/sales/daily?date. 매장 시간대 기준 일자와 주문 상태 포함 기준을 명시한다.
  // TODO-023: GET /api/admin/dashboard. summary와 중복 집계를 피하고 TODO-024/025이 소비할 단일
  // 응답 DTO를 확정한다.
  // 구현 순서: TODO-018 Mapper/XML → Service → 이 Controller → frontend TODO-019~025;
  // 각 endpoint는
  // query·빈 데이터·오류를 검증한다.
}
