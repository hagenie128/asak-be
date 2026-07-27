package com.asak.user.dto;

import java.util.List;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// menuList -> categories & menus 둘다 담아야함

// {
//   "data": {
//     "categories": [
//       {
//         "categoryId": 1,
//         "categoryName": "Burger",
//         "sortOrder": 1
//       }
//     ],
//     "menus": [
//       {
//         "menuId": 364,
//         "categoryId": 1,
//         "name": "Menu",
//         "price": 8900,
//         "imageUrl": "/assets/menu/364.png",
//         "isSoldOut": false,
//         "isOrderable": true
//       }
//     ]
//   }
// }

@Getter
@Setter
@Alias("menuList")
@NoArgsConstructor
public class MenuListResponse {

    private List<CategoryResponse> categories;
    private List<MenuListItemResponse> menus;

}
