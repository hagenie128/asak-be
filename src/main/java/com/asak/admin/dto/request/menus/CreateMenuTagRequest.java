package com.asak.admin.dto.request.menus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 메뉴 등록 시 menu_tag 연결용. code 우선, 없으면 name. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuTagRequest {

  private String code;
  private String name;
}
