package com.asak.user.dto.order.response;

import com.asak.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// {
//     "orderId":1,
//     "orderNo":"ASAK20260723000001",
//     "totalAmount":8900,
//     "status":"RECEIVED"
// }

@Getter
@Builder
public class CreateOrderResponse {

  private Long orderId;
  private String orderNo;
  private Integer totalAmount;
  private OrderStatus status;
}
