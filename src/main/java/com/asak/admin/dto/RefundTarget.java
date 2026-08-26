package com.asak.admin.dto;

import com.asak.common.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundTarget {

  private Long orderId;
  private Long paymentId;
  private String orderStatus;
  private String paymentStatus;
  private PaymentMethod paymentMethod;
  private int approvedAmount;
  private String providerPaymentKey;
}
