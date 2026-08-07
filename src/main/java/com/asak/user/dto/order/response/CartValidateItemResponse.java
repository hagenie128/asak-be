package com.asak.user.dto.order.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 주문 -> 1건의 주문 아이템에 대한 응답 그릇

//     "items": [
//       {
//         "clientCartItemId": "a1b2c3d4-...", 각각 장바구니에서의 개별 주문 id
//         "menuId": 364,
//         "quantity": 1,
//         "unitPrice": 8900, 장바구니 메뉴별(옵션 추가한 totalPrice)
//         "optionItems": [
//           {
//             "optionItemId": 269,
//             "quantity": 1
//           }
//         ],
//         "excludedIngredientIds": [],
//       }
//     ]

@Getter
@Setter
@NoArgsConstructor
@Alias("cartValidateItemResponse")
public class CartValidateItemResponse {

  private String clientCartItemId;
  private Long menuId;
  private Integer quantity;
  private Integer unitPrice; //장바구니 메뉴별(옵션 추가한 totalPrice)
  private List<CartValidateOptionItemResponse> optionItems;
  private List<Long> excludedIngredientIds;
}
