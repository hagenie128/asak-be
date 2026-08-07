package com.asak.user.dto.order.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 주문 1건에 대한 응답 dto

//   "data": {
//     "totalAmount": 8900,
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
//   }

@Getter
@Setter
@NoArgsConstructor
@Alias("cartValidateResponse")
public class CartValidateResponse {

  private Integer totalAmount; //주문건 총 totalAmount(각 메뉴별 가격 합)
  private List<CartValidateItemResponse> items;
}
