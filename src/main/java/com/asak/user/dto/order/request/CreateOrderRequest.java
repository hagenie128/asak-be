package com.asak.user.dto.order.request;

import com.asak.common.enums.OrderType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트앤드에서 받아온 request를 받는 그릇 역할

// {"orderType": "TAKE_OUT",
// "items": [
//       {"menuId": 364,
//       "quantity": 1,
//       "optionItems": [],
//       "excludedIngredientIds": []}
//     ]}

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {
  private OrderType orderType;
  private List<OrderItemRequest> items;
}
