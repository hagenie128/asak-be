package com.asak.admin.dto.request.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 옵션 그룹 내 항목 플래그. 추천 저장 시 사용. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuOptionItemRequest {

  private Long optionItemId;
  private Boolean isRecommended;
}
