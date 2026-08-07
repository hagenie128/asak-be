package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 품절 변경 한 건. targetType: MENU | INGREDIENT | OPTION_ITEM */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldOutChangeRequest {

  private String targetType;
  private Long targetId;
  private Boolean isSoldOut;
}
