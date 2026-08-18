package com.asak.user.dto.payment;

import com.asak.common.enums.OrderStatus;
import com.asak.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
    프론트엔드로부터 받는 requestBoby

        {
    "orderId": 1,
    "orderStatus": "READY",
    "paymentMethodCode": "CARD",
    "idempotencyKey": "uuid"
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
}
