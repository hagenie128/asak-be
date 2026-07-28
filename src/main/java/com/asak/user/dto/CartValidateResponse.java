package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//주문 1건에 대한 응답 dto

//   "data": {
//     "totalAmount": 8900,
//     "items": [
//       {
//         "menuId": 364,
//         "quantity": 1,
//         "unitAmount": 8900,
//         "optionItems": [
//           {
//             "optionItemId": 269,
//             "quantity": 1
//           }
//         ],
//         "excludedIngredientIds": []
//       }
//     ]
//   }

@Getter
@Setter
@NoArgsConstructor
@Alias("cartValidateResponse")
public class CartValidateResponse {

    private Integer totalAmount;
    private List<CartValidateItemResponse> items;


}
