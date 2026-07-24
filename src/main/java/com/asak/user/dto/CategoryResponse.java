package com.asak.user.dto;

import org.apache.ibatis.type.Alias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//카테고리 데이터 보내주는 그릇

//   "data": [
//     {
//       "categoryId": 1,
//       "categoryName": "Burger",
//       "sortOrder": 1,
//       "isActive": true
//     }
//   ]

@Getter
@Setter
@NoArgsConstructor
@Alias("category")
public class CategoryResponse {

    private Long categoryId;
    private String categoryName;
    private Integer sortOrder;
    private Boolean isActive;


}
