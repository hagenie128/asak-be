package com.asak.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 재료 선택 모달에서 검색·선택할 전체 재료 목록
public class IngredientListResponse {
  private Long ingredientId;
  private String name;
  private String type;
  private String unit;
  private boolean isSoldOut;
}
