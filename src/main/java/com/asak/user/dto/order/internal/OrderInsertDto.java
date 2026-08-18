package com.asak.user.dto.order.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/** orders 테이블 INSERT에 사용하는 내부 DTO. */
@Getter
@Setter
@Alias("OrderInsertDto")
@NoArgsConstructor
public class OrderInsertDto {

  private Long orderId;
  private String orderNo;
  private Long orderTypeId;
  private Long statusId;
  private Integer totalAmount;
}
