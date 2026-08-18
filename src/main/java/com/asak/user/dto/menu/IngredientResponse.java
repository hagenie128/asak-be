package com.asak.user.dto.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 재료

// {
//   "ingredientId": 1,
//   "ingName": "로메인",
//   "role": "base",
//   "unit": "g",
//   "isDefault": true,
//   "canRemove": true
// }

@Getter
@Setter
@Alias("ingredient")
@NoArgsConstructor
public class IngredientResponse {

  private Long ingredientId;
  private String ingName;
  private String role;
  private String unit;
  // 해당 메뉴에 기본 포함되는 재료인지
  private Boolean isDefault;
  // 해당 메뉴에서 제거 가능한 재료인지
  private Boolean canRemove;
}
