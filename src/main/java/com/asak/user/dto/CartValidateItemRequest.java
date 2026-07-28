package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 메뉴 1건에 대한 request (주문 -> "아이템 1개")

//  "items": [
//     {
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
public class CartValidateItemRequest {

    private Long menuId;
    private Integer quantity;
    private List<OptionItemRequest> optionItems;
    private List<Long> excludedIngredientIds;

}
