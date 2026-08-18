package com.asak.user.dto.payment.command;

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
}
