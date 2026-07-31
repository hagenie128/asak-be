package com.asak.user.dto.order.internal;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * order_item 테이블에 orderItem을 저장하기 위한  OrderItemInsertDto
 */
@Getter
@Setter
@NoArgsConstructor
@Alias("OrderItemInsertDto")
public class OrderItemInsertDto {

    private Long  orderItemId;
    private Long  orderId;
    private Long  menuId;
    private Integer  quantity;
    private Integer  unitPrice;


}
