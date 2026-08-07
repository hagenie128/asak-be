package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메뉴 등록 시 menu_ing 저장용 재료 한 줄. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuIngredientRequest {

  private Long ingredientId;
  /** CORE | BASE | DEFAULT (대소문자 무시). plain 은 DEFAULT 로 매핑. */
  private String role;
  private Double quantity;
  /** UNIT_TYPE 코드. 예: G */
  private String unit;
  private Boolean isDefault;
  private Boolean canRemove;
}
