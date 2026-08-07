package com.asak.admin.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메뉴 등록 시 menu_nutr 저장용. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuNutritionRequest {

  private BigDecimal kcal;
  private BigDecimal carbG;
  private BigDecimal proteinG;
  private BigDecimal fatG;
  private BigDecimal sodiumMg;
}
