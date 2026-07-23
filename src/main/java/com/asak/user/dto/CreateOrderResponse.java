package com.asak.user.dto;

import com.asak.common.enums.OrderStatus;

import lombok.Builder;
import lombok.Getter;

//   "data": {
//     "orderId": 1,
//     "orderNo": "A202607230001",
//     "totalAmount": 8900,
//     "status": "RECEIVED"
//   }

@Getter
@Builder
public class CreateOrderResponse {

    private Long orderId;
    private String orderNo;
    private Integer totalAmount;
    private OrderStatus status;

}
