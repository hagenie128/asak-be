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
public class OrderItemResponse {
  private Long menuId;
  private String menuName;
  private int quantity;
  private int unitPrice;

  @JsonRawValue private String optionItems; // vw_order_item_full.option_items 그대로 (JSON 배열 문자열)
  @JsonRawValue private String excludedIngredients; // vw_order_item_full.excluded_ingredients 그대로
}
