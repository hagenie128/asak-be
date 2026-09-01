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

  /** 이 대상을 품절하면 영향 받는 판매 중 메뉴 수. MVP는 count만 제공한다. */
  private int affectedMenuCount;
}
