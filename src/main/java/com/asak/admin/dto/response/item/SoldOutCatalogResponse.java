package com.asak.admin.dto.response.item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 품절 여부로 나눈 관리자 카탈로그. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldOutCatalogResponse {
  private List<SoldOutCatalogItemResponse> available;
  private List<SoldOutCatalogItemResponse> soldOut;
}
