package com.asak.admin.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOptPolicyResponse {
  private Long policyId;
  private String policyName;
  private int minSelect;
  private int maxSelect;
  private boolean isRequired;

  @JsonRawValue
  private String items; // vw_menu_opt_policy_json.items 그대로
}