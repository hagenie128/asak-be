package com.asak.admin.dto.response.menus;

import com.asak.admin.dto.response.item.OptionGroupSummaryResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDetailResponse {

  private Long menuId;
  private Long categoryId;
  private String categoryName;
  private String name;
  private int price;
  private String imageUrl;
  private String description;
  private boolean isSoldOut;
  private List<MenuIngredientSummaryResponse> ingredients;
  private List<OptionGroupSummaryResponse> optionGroups;
  private MenuNutritionResponse nutrition;
  private List<String> allergens;
  private List<String> tags;
}
