package com.asak.user.dto.payment;

import com.asak.common.enums.PaymentStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/*
    프론트엔드로부터 보내주는  response
        {
    "success": true,
    "status": 200,
    "code": "KIOSK_PAYMENT_APPROVED",
    "message": "결제가 승인되었습니다.",
    "data": {
        "paymentId": 1,
        "orderId": 1,
        "orderNo": "A202607230001",
        "paymentStatus": "APPROVED",
        "approvedAmount": 8900,
        "approvedAt": "2026-07-23T12:00:00",
        "waitingOrderCount": 0
    }
    }
*/



@Getter
@Setter
@NoArgsConstructor
@Alias("payment")
public class ApprovePaymentResponse {

  private Long paymentId;
  private Long orderId;
  private String orderNo;
  private PaymentStatus paymentStatus;
  private int approvedAmount;
  private LocalDateTime approvedAt;
  private int waitingOrderCount;
}
