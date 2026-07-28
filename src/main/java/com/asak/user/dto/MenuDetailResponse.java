package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 메뉴 디테일

// {
//   "menuId": 364,
//   "categoryId": 1,
//   "name": "닭가슴살 샐러드",
//   "price": 8900,
//   "imageUrl": "...",
//   "isSoldOut": false,

//   "ingredients": [
//     ...
//   ],

//   "optionGroups": [
//     ...
//   ],

//   "nutrition": {
//     "kcal": 430
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
    private Boolean isSoldOut;
    private List<IngredientResponse> ingredients;
    private List<OptionGroupResponse> optionGroups;
    private Nutrition nutrition;
}
