package com.asak.user.dto.menu;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 메뉴 디테일


//   "data": {
//     "menuId": 364,
//     "categoryId": 1,
//     "name": "Menu",
//     "price": 8900,
//     "imageUrl": "/assets/menu/364.png",
//     "description": "메뉴 설명",
//     "isSoldOut": false,
//     "ingredients": [
//       {
//         "ingredientId": 1,
//         "ingName": "로메인",
//         "role": "base",
//         "unit": "g",
//         "isDefault": true,
//         "canRemove": true
//       }
//     ],
//     "optionGroups": [
//       {
//         "optionGroupId": 240,
//         "name": "드레싱 선택",
//         "groupType": "DRESSING",
//         "selectType": "SINGLE",
//         "minSelect": 1,
//         "maxSelect": 1,
//         "sortOrder": 1,
//         "isRequired": true,
//         "items": [
//           {
//             "optionItemId": 269,
//             "ingredientId": 105,
//             "name": "크리미칠리",
//             "extraPrice": 0,
//             "originalPrice": null,
//             "servingAmount": 50,
//             "servingUnit": "g",
//             "isRecommended": true,
//             "isDefault": true,
//             "isSoldOut": false
//           }
//         ]
//       }
//     ],
//  "tags" : []
//   }
// }

@Getter
@Setter
@Alias("menuDetail")
@NoArgsConstructor
public class MenuDetailResponse {

  private Long menuId;
  private Long categoryId;
  private String name;
  private Integer price;
  private String imageUrl;
  private String description;
  private Boolean isSoldOut;
  private List<IngredientResponse> ingredients;
  private List<OptionGroupResponse> optionGroups;
  private List<String> tags;
}
