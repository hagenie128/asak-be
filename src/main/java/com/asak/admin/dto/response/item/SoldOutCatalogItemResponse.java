package com.asak.admin.dto.response.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관리자 품절 화면의 메뉴·재료·옵션 공통 행. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldOutCatalogItemResponse {
  private String targetType;
  private Long targetId;
  private String name;
  private String category;
  private boolean isSoldOut;
  private String imageUrl;
}
