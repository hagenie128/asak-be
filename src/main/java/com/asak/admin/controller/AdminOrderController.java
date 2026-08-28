package com.asak.admin.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.dto.request.orders.OrderRefundRequest;
import com.asak.admin.dto.response.orders.LiveOrderListResponse;
import com.asak.admin.dto.response.orders.OrderDetailResponse;
import com.asak.admin.dto.response.orders.OrderListResponse;
import com.asak.admin.service.AdminOrderService;
import com.asak.admin.service.AdminRefundReasonService;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.common.response.PageResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/admin/orders")
public class AdminOrderController {
  private final AdminOrderService adminOrderService;
  private final AdminRefundReasonService adminRefundReasonService;

  public AdminOrderController(
      AdminOrderService adminOrderService,
      AdminRefundReasonService adminRefundReasonService) {
    this.adminOrderService = adminOrderService;
    this.adminRefundReasonService = adminRefundReasonService;
  }

  // 조회 조건:

  // orderStatus IN (RECEIVED, PREPARING)
  // 정렬:

  // createdAt ASC (오래 기다린 주문 우선)
  // Empty는 오류가 아니라 200과 빈 content/목록이다.
  // Live 보드용 DTO는 화면 전용 menus[], 경과시간 등으로 조립할 수 있다. 주문 관리 목록/상세의 items[],
  // optionItems[]와 억지로 같은 DTO로 만들지 않는다.

  @GetMapping
  public ApiResponse<PageResult<OrderListResponse>> getOrders(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestParam(name = "orderStatus", required = false) String orderStatus,
      @RequestParam(name = "paymentStatus", required = false) String paymentStatus,
      @RequestParam(name = "orderType", required = false) String orderType,
      @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
      @RequestParam(name = "dateTo", required = false) LocalDate dateTo,
      @RequestParam(name = "keyword", required = false) String keyword) {
    PageResult<OrderListResponse> result = adminOrderService.getOrderList(
        page, size, orderStatus, paymentStatus, orderType, dateFrom, dateTo, keyword);
    return ApiResponse.success("ADMIN_ORDER_LIST_SUCCESS", "관리자 주문 목록 조회 성공", result);
  }

  @GetMapping("/{orderId}")
  public ApiResponse<OrderDetailResponse> getOrderDetail(
      @PathVariable(name = "orderId") Long orderId) {
    OrderDetailResponse result = adminOrderService.getOrderDetail(orderId);
    if (result == null) {
      return ApiResponse.error(ErrorCode.ORDER_NOT_FOUND);
    }
    return ApiResponse.success("ADMIN_ORDER_DETAIL_SUCCESS", "관리자 주문 상세 조회 성공", result);
  }

  @GetMapping("/live")
  public ApiResponse<LiveOrderListResponse> getLiveOrders() {
    // Empty(0건)는 오류가 아님 — 200 + 빈 content. NOT_FOUND는 특정 orderId 조회 실패에만 사용.
    LiveOrderListResponse result = adminOrderService.getLiveOrders();
    return ApiResponse.success("ADMIN_LIVE_ORDERS_SUCCESS", "관리자 Live 주문 조회 성공", result);
  }

  // | API-008 | `PATCH /api/admin/orders/{orderId}/status` | 허용 상태 전이와 동시 변경 충돌
  // 처리 | 404, 409 상태 전이 충돌 |

  @PatchMapping("/{orderId}/{status}")
  public ApiResponse<Void> changeOrderStatus(
      @PathVariable(name = "orderId") Long orderId, @PathVariable(name = "status") String status) {
    OrderDetailResponse response = adminOrderService.getOrderDetail(orderId);
    if (response == null) {
      return ApiResponse.error(ErrorCode.ORDER_NOT_FOUND);
    }

    // Service가 규칙 위반 / 동시성 충돌을 구분해 돌려준다.
    return switch (adminOrderService.changeOrderStatus(response, status)) {
      case SUCCESS ->
        ApiResponse.success("ADMIN_ORDER_STATUS_CHANGE_SUCCESS", "관리자 주문 상태 변경 성공", null);
      case INVALID_TRANSITION -> ApiResponse.error(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
      case CONFLICT -> ApiResponse.error(ErrorCode.ORDER_STATUS_CONFLICT);
    };
  }

  // | API-024 | `PATCH /api/admin/orders/{orderId}/cancel` | 주문 취소·승인 결제 환불·시각 저장
  // | 409 `ORDER_CANCEL_NOT_ALLOWED` |
  @PatchMapping("/{orderId}/cancel")
  public ApiResponse<Void> cancelOrder(@PathVariable(name = "orderId") Long orderId) {
    OrderDetailResponse response = adminOrderService.getOrderDetail(orderId);
    if (response == null) {
      return ApiResponse.error(ErrorCode.ORDER_NOT_FOUND);
    } else if (response.getPaymentStatus().equals("APPROVED")) {
      return ApiResponse.error(ErrorCode.ORDER_PAYMENT_APPROVED_CANCEL_NOT_ALLOWED);
    } else if (response.getOrderStatus().equals("COMPLETED")
        || response.getOrderStatus().equals("CANCELED")) {
      return ApiResponse.error(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
    try {
      adminOrderService.cancelOrder(orderId);
      return ApiResponse.success("ADMIN_ORDER_CANCEL_SUCCESS", "관리자 주문 취소 성공", null);
    } catch (CustomException e) {
      return ApiResponse.error(e.getErrorCode());
    }
  }

  // TODO-038: 환불은 cancel과 분리한 PATCH /api/admin/orders/{orderId}/refund 계약으로 구현한다.
  // 1) 카드/신용카드는 이번 범위에 포함한다. 토스페이는 실제 API 연동·결제 과정 통합 테스트가 성공할 때만 포함한다.
  @PatchMapping("/{orderId}/refund")
  public ApiResponse<OrderDetailResponse> refundOrder(
      @PathVariable(name = "orderId") Long orderId,
      @RequestBody @Valid OrderRefundRequest request) {

    String refundReasonText =
        adminRefundReasonService.resolveRefundReasonText(
            request.getRefundReasonCode(), request.getRefundReasonDetail());

    OrderDetailResponse result = adminOrderService.refundOrder(orderId, refundReasonText);

    return ApiResponse.success(
        "ADMIN_ORDER_REFUND_SUCCESS",
        "관리자 주문 환불 성공",
        result);
  }

  // TODO-040/042와 연결한다.
}
