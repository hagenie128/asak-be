package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 품절 변경 Request — PATCH /api/admin/soldOut */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldOutPatchRequest {

  private String targetType;
  private Long targetId;
  private Boolean isSoldOut;
}
