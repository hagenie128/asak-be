package com.asak.admin.dto.response.item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메뉴 상세 화면의 옵션 그룹 표시용 요약 정보입니다. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupSummaryResponse {

  private Long optionGroupId;
  private String name;
  private String groupType;
  private String selectType;
  private Integer minSelect;
  private Integer maxSelect;
  private Boolean isRequired;
  private String recommendedLabel;
  private List<OptionItemSummaryResponse> items;
}
