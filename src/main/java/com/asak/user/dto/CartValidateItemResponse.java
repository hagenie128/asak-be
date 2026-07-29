package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//주문 -> 1건의 주문 아이템에 대한 응답 그릇

//     "items": [
//       {
//         "menuId": 364,
//         "quantity": 1,
//         "unitPrice": 8900,
//         "optionItems": [
//           {
//             "optionItemId": 269,
//             "quantity": 1
//           }
//         ],
//         "excludedIngredientIds": []
//       }
//     ]

@Getter
@Setter
@NoArgsConstructor
@Alias("cartValidateItemResponse")
public class CartValidateItemResponse {

    private Long menuId;
    private Integer quantity;
    private Integer unitPrice;
    private List<CartValidateOptionItemResponse> optionItems;
    private List<Long> excludedIngredientIds;

}
