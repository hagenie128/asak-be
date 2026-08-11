package com.asak.admin.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IngredientResponse {
  private Long id;
  private String name;
  private boolean isSoldOut;
  private String roleName;
  private String unitName;
  private Double servingG;
  private Double kcal;
  private Double carbG;
  private Double sugarG;
  private Double proteinG;
  private Double fatG;
  private Double saturatedFatG;
  private Double sodiumMg;
}
