package com.asak.admin.mapper;

import java.util.Map;

import com.asak.admin.dto.RefundTarget;

public interface AdminPaymentMapper {

  int cancelOrderForRefund(Map<String, Object> map);

  int insertPaymentRefund(Map<String, Object> refundMap);

  int markPaymentRefunded(Map<String, Object> paymentMap);

  RefundTarget findRefundTarget(Long orderId);
}
