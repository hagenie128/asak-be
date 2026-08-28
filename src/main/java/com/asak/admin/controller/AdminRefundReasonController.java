package com.asak.admin.controller;

import com.asak.admin.dto.response.orders.RefundReasonResponse;
import com.asak.admin.service.AdminRefundReasonService;
import com.asak.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/refund-reasons")
public class AdminRefundReasonController {

  private final AdminRefundReasonService adminRefundReasonService;

  public AdminRefundReasonController(AdminRefundReasonService adminRefundReasonService) {
    this.adminRefundReasonService = adminRefundReasonService;
  }

  @GetMapping
  public ApiResponse<List<RefundReasonResponse>> getRefundReasons() {
    List<RefundReasonResponse> reasons = adminRefundReasonService.getRefundReasons();
    return ApiResponse.success(
        "ADMIN_REFUND_REASON_LIST_SUCCESS", "환불 사유 목록 조회 성공", reasons);
  }
}
