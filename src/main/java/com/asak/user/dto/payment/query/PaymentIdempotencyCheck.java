package com.asak.user.dto.payment.query;

import com.asak.common.enums.PaymentMethod;
import com.asak.common.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 결제 조회 결과를 담는 내부 DTO

@Getter
@Setter
@NoArgsConstructor
@Alias("IdempotencyCheckDTO")
public class PaymentIdempotencyCheck {

  private Long paymentId; // 기존 승인 결과 조회
  private Long orderId; // 같은 주문 요청인지 비교
  private PaymentMethod paymentMethodCode; // 같은 결제 요청인지 비교
  private PaymentStatus paymentStatus; // 승인 완료/실패/처리 상태 판단
}
