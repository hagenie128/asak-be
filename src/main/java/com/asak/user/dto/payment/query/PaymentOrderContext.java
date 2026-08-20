package com.asak.user.dto.payment.query;

import com.asak.common.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 결제 대상 주문 조회
@Getter
@Setter
@NoArgsConstructor
@Alias("PaymentOrderContext")
public class PaymentOrderContext {

  private OrderStatus orderStatus;
  private String orderNo;
  private int totalPrice;
}
