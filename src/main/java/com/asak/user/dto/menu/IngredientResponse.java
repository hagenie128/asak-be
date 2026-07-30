package com.asak.user.dto.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 재료

// [{"ingredientId": 1,
// ]"ingName": "로메인",
// "role": "base",  재료 역할 (예: base, main, topping)
// "unit": "g", 단위

@Getter
@Setter
@Alias("ingredient")
@NoArgsConstructor
public class IngredientResponse {

  private Long ingredientId;
  private String ingName;
  private String role;
  private String unit;
}
