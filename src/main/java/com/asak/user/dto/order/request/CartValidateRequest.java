package com.asak.user.dto.order.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 주문 1건에 대한 request 장바구니 검증

@Getter
@Setter
@NoArgsConstructor
@Alias("cart")
public class CartValidateRequest {

  private List<CartValidateItemRequest> items;
}
