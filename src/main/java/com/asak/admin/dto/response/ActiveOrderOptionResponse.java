package com.asak.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveOrderOptionResponse {

  private Long optionItemId;
  private String optionName;
  private int quantity;
}