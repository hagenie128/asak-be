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
public class OptionGroupPolicyResponse {
  private Long optionGroupId;
  private String name;
  private String groupType;
  private String selectType;
  private int minSelect;
  private int maxSelect;
  private int sortOrder;
  private boolean isRequired;

  @JsonRawValue
  private String items; // vw_menu_opt_policy_json.items 그대로
}