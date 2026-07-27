package com.asak.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메뉴 상세 화면의 재료 표시와 수정 초기값에 필요한 최소 정보입니다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuIngredientSummaryResponse {

  private Long ingredientId;
  private String name;
  private boolean isSoldOut;
  private String role;
  private double quantity;
  private String unit;
  private boolean isDefault;
  private boolean canRemove;
}
