package com.asak.admin.service;

import com.asak.admin.dto.RefundTarget;
import com.asak.admin.dto.request.orders.OrderListFilter;
import com.asak.admin.dto.response.orders.LiveOrderListResponse;
import com.asak.admin.dto.response.orders.LiveOrderResponse;
import com.asak.admin.dto.response.orders.OrderDetailResponse;
import com.asak.admin.dto.response.orders.OrderListResponse;
import com.asak.admin.mapper.AdminOrderMapper;
import com.asak.admin.mapper.AdminPaymentMapper;
import com.asak.admin.mapper.AdminPaymentMethodMapper;
import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentStatus;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.PageResult;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminOrderService {

  private static final int MAX_ORDER_LIST_SIZE = 100;

  /** 상태변경 결과 — Controller가 ErrorCode로 매핑한다. */
  public enum StatusChangeResult {
    SUCCESS,
    /** 규칙상 허용되지 않는 전이 (예: RECEIVED→COMPLETED) */
    INVALID_TRANSITION,
    /** 조회 시점과 DB 상태가 달라 UPDATE 0건 */
    CONFLICT
  }

  private final AdminOrderMapper adminOrderMapper;
  private final AdminPaymentMethodMapper adminPaymentMethodMapper;
  private final PaymentService paymentService;
  private final AdminPaymentMapper adminPaymentMapper;
  private final AdminRefundTransactionService refundTransactionService;

  public AdminOrderService(
      AdminOrderMapper adminOrderMapper,
      AdminPaymentMethodMapper adminPaymentMethodMapper,
      PaymentService paymentService,
      AdminPaymentMapper adminPaymentMapper,
      AdminRefundTransactionService refundTransactionService) {
    this.adminOrderMapper = adminOrderMapper;
    this.adminPaymentMethodMapper = adminPaymentMethodMapper;
    this.paymentService = paymentService;
    this.adminPaymentMapper = adminPaymentMapper;
    this.refundTransactionService = refundTransactionService;
  }

  public LiveOrderListResponse getLiveOrders() {
    List<LiveOrderResponse> content = adminOrderMapper.getLiveOrders();
    return LiveOrderListResponse.builder().content(content).totalElements(content.size()).build();
  }

  public PageResult<OrderListResponse> getOrderList(
      int page,
      int size,
      String status,
      String paymentStatus,
      String orderType,
      LocalDate dateFrom,
      LocalDate dateTo,
      String keyword) {
    int safeSize = Math.min(Math.max(1, size), MAX_ORDER_LIST_SIZE);
    long totalElements;

    OrderListFilter mapperFilter =
        OrderListFilter.builder()
            .status(blankToNull(status))
            .paymentStatus(blankToNull(paymentStatus))
            .orderType(blankToNull(orderType))
            .startAt(dateFrom == null ? null : dateFrom.atStartOfDay())
            .endAt(dateTo == null ? null : dateTo.plusDays(1).atStartOfDay())
            .keyword(blankToNull(keyword))
            .limit(safeSize)
            .offset(0)
            .build();

    totalElements = adminOrderMapper.countOrderList(mapperFilter);
    int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / safeSize));
    int safePage = Math.min(Math.max(0, page), totalPages - 1);

    OrderListFilter pagedFilter =
        OrderListFilter.builder()
            .status(mapperFilter.getStatus())
            .paymentStatus(mapperFilter.getPaymentStatus())
            .orderType(mapperFilter.getOrderType())
            .startAt(mapperFilter.getStartAt())
            .endAt(mapperFilter.getEndAt())
            .keyword(mapperFilter.getKeyword())
            .limit(safeSize)
            .offset(safePage * safeSize)
            .build();

    List<OrderListResponse> content = adminOrderMapper.getOrderList(pagedFilter);
    return new PageResult<>(content, safePage, safeSize, totalElements);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  public OrderDetailResponse getOrderDetail(Long orderId) {
    return adminOrderMapper.getOrderDetail(orderId);
  }

  /**
   * MVP 허용 전이: RECEIVED→PREPARING, PREPARING→COMPLETED. 규칙 위반은 DB를 치기 전에 INVALID_TRANSITION. 규칙은
   * 맞는데 UPDATE 0건이면 CONFLICT(다른 요청이 먼저 변경).
   */
  public StatusChangeResult changeOrderStatus(OrderDetailResponse response, String status) {
    String current = response.getOrderStatus();
    if (!isAllowedTransition(current, status)) {
      return StatusChangeResult.INVALID_TRANSITION;
    }

    Long expectedStatusId = adminOrderMapper.findOrderStatusId(current);
    Long nextStatusId = adminOrderMapper.findOrderStatusId(status);
    if (expectedStatusId == null || nextStatusId == null) {
      return StatusChangeResult.INVALID_TRANSITION;
    }

    Map<String, Object> map = new HashMap<>();
    map.put("orderId", response.getOrderId());
    map.put("statusId", nextStatusId);
    map.put("expectedStatusId", expectedStatusId);

    int updated = adminOrderMapper.changeOrderStatus(map);
    if (updated == 0) {
      return StatusChangeResult.CONFLICT;
    }
    return StatusChangeResult.SUCCESS;
  }

  private boolean isAllowedTransition(String current, String next) {
    if (current == null || next == null) {
      return false;
    }
    if (OrderStatus.RECEIVED.name().equals(current) && OrderStatus.PREPARING.name().equals(next)) {
      return true;
    }
    if (OrderStatus.PREPARING.name().equals(current) && OrderStatus.COMPLETED.name().equals(next)) {
      return true;
    }
    return false;
  }

  public int cancelOrder(Long orderId) throws CustomException {
    try {
      // TODO-001: 승인 결제의 취소 정책.
      // 2) 환불은 외부 결제 취소 성공 후 TODO-039의 payment REFUNDED와 order CANCELED 갱신을 하나의
      // 트랜잭션으로 처리한다.
      // 3) 카드/신용카드는 이번 범위이고, 토스페이는 실제 연동·결제 과정 통합 테스트 성공이 포함 조건이다.
      // 4) 0건 갱신, 허용하지 않는 상태, 동시 변경은 성공으로 숨기지 않고 ErrorCode로 구분한다.
      // APPROVED·COMPLETED·CANCELED 는 취소 불가
      OrderDetailResponse response = adminOrderMapper.getOrderDetail(orderId);
      if (response == null) {
        throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
      }
      // 취소 불가: READY(키오스크 결제 대기), APPROVED 결제, COMPLETED, CANCELED
      if (OrderStatus.READY.name().equals(response.getOrderStatus())) {
        throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
      }
      if (PaymentStatus.APPROVED.name().equals(response.getPaymentStatus())) {
        throw new CustomException(ErrorCode.ORDER_PAYMENT_APPROVED_CANCEL_NOT_ALLOWED);
      }
      if (OrderStatus.COMPLETED.name().equals(response.getOrderStatus())) {
        throw new CustomException(ErrorCode.ORDER_COMPLETED_CANCEL_NOT_ALLOWED);
      }
      if (OrderStatus.CANCELED.name().equals(response.getOrderStatus())) {
        throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELED);
      }
      Map<String, Object> map = new HashMap<>();
      map.put("canceledStatusId", adminOrderMapper.findOrderStatusId("CANCELED"));
      if (map.get("canceledStatusId") == null) {
        throw new CustomException(ErrorCode.ORDER_STATUS_NOT_FOUND);
      }
      map.put("orderId", response.getOrderId());
      int updated = adminOrderMapper.cancelOrder(map);
      if (updated == 0) {
        throw new CustomException(ErrorCode.ORDER_CANCEL_FAILED);
      }
      return updated;
    } catch (CustomException e) {
      throw e;
    }
  }

  public OrderDetailResponse refundOrder(long orderId, String refundReason) {

    if (adminOrderMapper.getOrderDetail(orderId) == null) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }

    RefundTarget target = adminPaymentMapper.findRefundTarget(orderId);

    if (target == null) {
      throw new CustomException(ErrorCode.ONLY_APPROVED_PAYMENT_CAN_BE_REFUNDED);
    }

    if (!PaymentStatus.APPROVED.name().equals(target.getPaymentStatus())) {

      throw new CustomException(ErrorCode.ONLY_APPROVED_PAYMENT_CAN_BE_REFUNDED);
    }

    if (OrderStatus.CANCELED.name().equals(target.getOrderStatus())) {

      throw new CustomException(ErrorCode.CANCELED_ORDER_CANNOT_BE_REFUNDED);
    }

    if (target.getApprovedAmount() <= 0) {
      throw new CustomException(ErrorCode.AMOUNT_INVALID_NOT_ALLOWED);
    }

    // ==========================
    // 트랜잭션 밖
    // 가상/외부 PG 환불
    // ==========================

    String cancelTransactionKey;

    switch (target.getPaymentMethod()) {
      case CARD:
        cancelTransactionKey = paymentService.cardRefund(target);
        break;
      // case KAKAO_PAY:
      // paymentService.refundKakaoPayOrder(target);
      // break;
      // case TOSS_PAY:
      // paymentService.refundTossPayOrder(target);
      // break;
      // case NAVER_PAY:
      // paymentService.refundNaverPayOrder(target);
      // break;
      default:
        throw new CustomException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED_FOR_REFUND);
    }

    // ==========================
    // 여기서부터 별도 Bean의 Transaction
    // ==========================

    refundTransactionService.applyRefund(target, refundReason, cancelTransactionKey);

    return adminOrderMapper.getOrderDetail(orderId);
  }
}
