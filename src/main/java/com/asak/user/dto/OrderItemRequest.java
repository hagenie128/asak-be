package com.asak.user.dto;

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

// -> API-004(CartValidateRequest), API-005(CreateOrderRequest) 공용으로 씀

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {

  private Long menuId;
  private Integer quantity;
  private List<OptionItemRequest> optionItems = new ArrayList<>();
  private List<Long> excludedIngredientIds = new ArrayList<>();
}
