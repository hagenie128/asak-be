package com.asak.user.dto;

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

@Getter
@Setter
@NoArgsConstructor
public class OrderItemRequest {

  private Long menuId;
  private Integer quantity;
  private List<OptionItemRequest> optionItems;
  private List<Long> excludedIngredientIds;
}
