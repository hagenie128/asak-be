package com.asak.admin.controller;

import com.asak.admin.dto.request.UpdatePaymentMethodRequest;
import com.asak.admin.dto.response.AdminPaymentMethodResponse;
import com.asak.admin.service.AdminPaymentMethodService;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-011 [구현 완료 · API/DB 검증 대기]: GET 목록과 PATCH /{methodId}는 구현됐고,
// 식별자는 path variable만 사용한다. PATCH 필드는 active, sortNo이며 receiptMessage는 계약에 포함하지 않는다.
// Service는 없는 id·유효하지 않은 요청·0건 갱신을 ErrorCode로 구분한다.
// GET 정렬, PATCH 뒤 재조회, 없는 id·유효하지 않은 요청·0건 갱신은 실제 API와 DB에서 확인해야 한다.

@RestController
@RequestMapping("/api/admin/paymentMethods")
public class AdminPaymentMethodController {

  private final AdminPaymentMethodService adminPaymentMethodService;

  public AdminPaymentMethodController(AdminPaymentMethodService adminPaymentMethodService) {
    this.adminPaymentMethodService = adminPaymentMethodService;
  }

  @GetMapping
  public ApiResponse<List<AdminPaymentMethodResponse>> getPaymentMethods() {
    List<AdminPaymentMethodResponse> response = adminPaymentMethodService.getPaymentMethods();
    return ApiResponse.success(response);
  }

  @PatchMapping("/{methodId}")
  public ApiResponse<String> updatePaymentMethod(
      @PathVariable(name = "methodId") Long methodId,
      @RequestBody UpdatePaymentMethodRequest request) {
    try {
      int result = adminPaymentMethodService.updatePaymentMethod(methodId, request);
      if (result == 0) {
        return ApiResponse.error(ErrorCode.PAYMENT_METHOD_UPDATE_FAILED);
      }
      return ApiResponse.success("결제수단 수정 성공");
    } catch (CustomException e) {
      return ApiResponse.error(e.getErrorCode());
    }
  }
}
