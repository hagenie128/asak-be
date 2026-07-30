package com.asak.user.dto.menu;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

// 메뉴 옵션 그룹

// [{"optionGroupId": 240,
// "name": "드레싱 선택",
//  "groupType": "DRESSING",
//  "selectType": "SINGLE", //단일 선택 || 복수 선택
//  "minSelect": 1,
//  "maxSelect": 1,
//  "sortOrder": 1,
//  "isRequired": true,
//  "items": [ 옵션 아이템 ... ]

@Getter
@Setter
@Alias("optionGroup")
@NoArgsConstructor
public class OptionGroupResponse {

  private Long optionGroupId;
  private String name;
  private String groupType;
  private String selectType;
  private Integer minSelect;
  private Integer maxSelect;
  private Integer sortOrder;
  private Boolean isRequired;
  private List<OptionItemResponse> items;
}
