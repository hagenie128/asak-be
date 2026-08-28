package com.asak.admin.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asak.admin.dto.RefundTarget;
import com.asak.admin.mapper.AdminOrderMapper;
import com.asak.admin.mapper.AdminPaymentMapper;
import com.asak.admin.mapper.AdminPaymentMethodMapper;
import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentStatus;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;

@Service
public class AdminRefundTransactionService {

  private final AdminPaymentMethodMapper adminPaymentMethodMapper;
  private final AdminPaymentMapper adminPaymentMapper;
  private final AdminOrderMapper adminOrderMapper;

  public AdminRefundTransactionService(
      AdminPaymentMethodMapper adminPaymentMethodMapper,
      AdminPaymentMapper adminPaymentMapper,
      AdminOrderMapper adminOrderMapper) {

    this.adminPaymentMethodMapper = adminPaymentMethodMapper;
    this.adminPaymentMapper = adminPaymentMapper;
    this.adminOrderMapper = adminOrderMapper;
  }

  @Transactional
  public void applyRefund(
      RefundTarget target,
      String refundReason,
      String providerCancelTransactionKey) {

    Long approvedStatusId = adminPaymentMethodMapper.findPaymentMethodStatusId(
        PaymentStatus.APPROVED.name());

    Long refundedStatusId = adminPaymentMethodMapper.findPaymentMethodStatusId(
        PaymentStatus.REFUNDED.name());

    if (approvedStatusId == null || refundedStatusId == null) {
      throw new CustomException(
          ErrorCode.PAYMENT_METHOD_STATUS_NOT_FOUND);
    }

    // 1. payment : APPROVED -> REFUNDED
    Map<String, Object> paymentMap = new HashMap<>();

    paymentMap.put("paymentId", target.getPaymentId());
    paymentMap.put("approvedStatusId", approvedStatusId);
    paymentMap.put("refundedStatusId", refundedStatusId);

    int paymentUpdated = adminPaymentMapper.markPaymentRefunded(paymentMap);

    if (paymentUpdated != 1) {
      throw new CustomException(
          ErrorCode.ORDER_REFUND_FAILED);
    }

    // 2. payment_refund 이력 INSERT
    Map<String, Object> refundMap = new HashMap<>();

    refundMap.put("paymentId", target.getPaymentId());
    refundMap.put("amount", target.getApprovedAmount());
    refundMap.put("refundReason", refundReason);
    refundMap.put(
        "providerCancelTransactionKey",
        providerCancelTransactionKey);
    refundMap.put("refundedAt", LocalDateTime.now());

    int refundInserted = adminPaymentMapper.insertPaymentRefund(refundMap);

    if (refundInserted != 1) {
      throw new CustomException(
          ErrorCode.ORDER_REFUND_FAILED);
    }

    // 3. 제공 완료 주문은 COMPLETED 유지
    if (OrderStatus.COMPLETED.name()
        .equals(target.getOrderStatus())) {
      return;
    }

    // 제공 전 환불이면 CANCELED
    Long canceledStatusId = adminOrderMapper.findOrderStatusId(
        OrderStatus.CANCELED.name());

    if (canceledStatusId == null) {
      throw new CustomException(
          ErrorCode.ORDER_STATUS_NOT_FOUND);
    }

    Map<String, Object> orderMap = new HashMap<>();

    orderMap.put("orderId", target.getOrderId());
    orderMap.put("canceledStatusId", canceledStatusId);

    int orderUpdated = adminPaymentMapper.cancelOrderForRefund(orderMap);

    if (orderUpdated != 1) {
      throw new CustomException(
          ErrorCode.ORDER_REFUND_FAILED);
    }
  }
}
