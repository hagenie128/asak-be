package com.asak.user.dto.payment.command;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 결제 INSERT DTO
@Getter
@Setter
@NoArgsConstructor
public class PaymentInsertCommand {

  // INSERT 후 DB에서 생성된 PK가 들어옴
  private Long paymentId;
  private Long orderId;
  private Long methodId;
  private int amount;
  private String idempotencyKey;
  private String providerPaymentKey; //토스페이먼츠로부터 발급 받는 키값
  private OffsetDateTime approvedAt; //토스페이먼츠로부터 승인된 시각 저장
}
