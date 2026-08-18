package com.asak.admin.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 품절 일괄 변경 Request — PATCH /api/admin/soldOut Bruno · FE types/soldOut.js 의 changes[] 와 동일. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldOutPatchRequest {

  private List<SoldOutChangeRequest> changes;
}
