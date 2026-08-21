package com.asak.user.dto.payment;

import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentMethod;
import com.asak.user.dto.payment.query.TossPaymentAuth;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
    api-006 프론트엔드로부터 받는 requestBoby
    {
      "orderId": 1,
      "orderStatus": "RECEIVED",
      "paymentMethodCode": "TOSS_PAY",
      "idempotencyKey": "uuid",
      -- 토스페이먼츠 필수요구 request --
      "tossPayment": {
        "paymentKey": "tgen_20260819...",
        "orderId": "A202607230001",
        "amount": 8900
      }
    }


*/

@Getter
@Setter
@NoArgsConstructor
public class ApprovePaymentRequest {

  private Long orderId;
  private OrderStatus orderStatus;
  private PaymentMethod paymentMethodCode; // CARD, | KAKAO_PAY, |NAVER_PAY
  private String idempotencyKey;

  @Valid private TossPaymentAuth tossPayment;
}
