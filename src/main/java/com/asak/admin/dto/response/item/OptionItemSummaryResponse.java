package com.asak.admin.dto.response.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메뉴·옵션 그룹에 연결된 옵션 항목. 추천 선택과 품절 표시에 사용한다. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionItemSummaryResponse {

  private Long optionItemId;
  private String name;
  private Integer extraPrice;
  private Boolean isSoldOut;
  private Boolean isRecommended;
}
