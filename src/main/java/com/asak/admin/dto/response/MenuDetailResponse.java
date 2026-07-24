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
public class MenuDetailResponse {

  private Long menuId;
  private Long catId;
  private String catName;
  private String name;
  private int price;
  private String imageUrl;
  private String description;
  private boolean isSoldOut;
  private List<MenuIngResponse> ingredients;
  private List<MenuOptPolicyResponse> optionPolicies;
}