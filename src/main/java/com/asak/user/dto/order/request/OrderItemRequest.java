package com.asak.user.dto.order.request;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// {"orderType": "TAKE_OUT",
// "items": [
//       {"menuId": 364,
//       "quantity": 1,
//       "optionItems": [],
//       "excludedIngredientIds": []}
//     ]}

// API-005(CreateOrderRequest)의 주문 아이템 요청 DTO.
// API-004는 CartValidateItemRequest를 사용하고, 내부 OptionItemRequest 형식만 API-005와 공유한다.

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {

  private Long menuId;
  private Integer quantity;
  private List<OptionItemRequest> optionItems = new ArrayList<>();
  private List<Long> excludedIngredientIds = new ArrayList<>();
}
