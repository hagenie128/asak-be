package com.asak.admin.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrderItemResponse {

  private Long orderItemId;
  private Long menuId;
  private String menuName;
  private int quantity;
  private List<ActiveOrderOptionResponse> selectedOptions;
  private List<ActiveOrderExcludedIngredientResponse> excludedIngredients;
}