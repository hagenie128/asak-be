package com.asak.admin.dto.request.item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메뉴 등록 시 menu_opt_policy 연결 + 메뉴별 추천 옵션(menu_opt_override). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuOptionGroupRequest {

  private Long optionGroupId;
  private Boolean isRequired;

  /** 메뉴별 추천 옵션. 있으면 menu_opt_override.recommended 로 저장. */
  private Long recommendedOptionItemId;

  /** FE payload 호환: items[].isRecommended 로 recommendedOptionItemId 를 추론할 수 있다. */
  private List<CreateMenuOptionItemRequest> items;
}
