package com.asak.user.dto.order.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 주문 1개건의 -> 1개의 아이템의 옵션 아이템 응답

//         "optionItems": [
//           {
//             "optionItemId": 269,
//             "quantity": 1
//           }
//         ],

@Getter
@Setter
@NoArgsConstructor
@Alias("cartValidateOptionItemResponse")
public class CartValidateOptionItemResponse {

  private Long optionItemId;
  private Integer quantity;
}
