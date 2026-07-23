package com.asak.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrderExcludedIngredientResponse {

  private Long ingredientId;
  private String ingredientName;
}