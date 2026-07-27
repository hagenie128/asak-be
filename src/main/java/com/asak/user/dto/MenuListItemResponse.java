package com.asak.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// "menus": [
// {
// "menuId": 364,
// "categoryId": 1,
// "name": "Menu",
// "price": 8900,
// "imageUrl": "/assets/menu/364.png",
// "isSoldOut": false,
// "isOrderable": true
// }
// ]
// }
// }

@Getter
@Setter
@NoArgsConstructor
public class MenuListItemResponse {

    private Long menuId;
    private Long categoryId;
    private String name;
    private Integer price;
    private String imageUrl;
    private Boolean isSoldOut;
    private Boolean isOrderable;

}
