package com.asak.user.dto.payment.tossPayment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 백엔드가 토스로부터 보내는 토스 승인 요청 dto

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentConfirmRequest {
  private String paymentKey;
  private String orderId;
  private Long amount;
}
