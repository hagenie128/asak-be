package com.asak.user.dto.menu;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// "menus": [
// {
// "menuId": 364,
// "categoryId": 1,
// "name": "Menu",
// "price": 8900,
// "imageUrl": "/assets/menu/364.png",
// "isSoldOut": false,
// "isOrderable": true,
//  "kcal" : 50
// }
// ]
// }
// }

@Getter
@Setter
@NoArgsConstructor
@Alias("menuList")
public class MenuListItemResponse {

  private Long menuId;
  private Long categoryId;
  private String name;
  private Integer price;
  private String imageUrl;
  private Double kcal;
  private Boolean isSoldOut;
  private Boolean isOrderable;
  private List<String> tags = new ArrayList<>();
}
