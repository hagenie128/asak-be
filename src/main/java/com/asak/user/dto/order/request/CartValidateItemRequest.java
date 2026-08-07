package com.asak.user.dto.order.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 메뉴 1건에 대한 request (주문 -> "아이템 1개")

//  "items": [
//     {
//       "clientCartItemId": "a1b2c3d4-...", 각각 장바구니에서의 개별 주문 id
//       "menuId": 364,
//       "quantity": 1,
//       "optionItems": [
//         {
//           "optionItemId": 269,
//           "quantity": 1
//         }
//       ],
//       "excludedIngredientIds": [
//         1
//       ]
//     }
//   ]

@Getter
@Setter
@NoArgsConstructor
@Alias("cartItem")
public class CartValidateItemRequest implements OrderItemCommand {

  private String clientCartItemId;
  private Long menuId;
  private Integer quantity;
  private List<OptionItemRequest> optionItems;
  private List<Long> excludedIngredientIds;
}
