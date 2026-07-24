package com.asak.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuIngResponse {
  private Long ingredientId;
  private String name;
  private boolean isSoldOut;
  private String role;
  private double quantity;
  private String unit;
  private boolean isDefault;
  private boolean canRemove;
  private int sortNo;

  @JsonRawValue
  private String allergens; // vw_menu_ing_json.allergens 그대로
}