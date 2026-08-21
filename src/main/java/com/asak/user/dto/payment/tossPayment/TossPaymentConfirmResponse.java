package com.asak.user.dto.payment.tossPayment;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 토스페이먼츠로부터 백엔드에게 승인 받은 response 응답

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentConfirmResponse {

  private String status; // DONE인지 확인
  private String paymentKey; // 토스페이먼츠가 부여하는 db저장키
  private String orderId; // 재검증
  private Integer totalAmount; // DB 주문금액과 재검증
  private OffsetDateTime approvedAt;
}
