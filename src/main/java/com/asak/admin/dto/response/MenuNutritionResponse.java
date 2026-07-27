package com.asak.admin.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuNutritionResponse {
  private BigDecimal kcal;
  private BigDecimal carbG;
  private BigDecimal proteinG;
  private BigDecimal fatG;
  private BigDecimal sodiumMg;
}
