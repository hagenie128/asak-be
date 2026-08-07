package com.asak.admin.dto.response;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 카테고리 목록 행.
 * GET /api/admin/menus/categories → PageResult.content[]
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Alias("adminCategory")
public class AdminCategoryResponse {

  private Long categoryId;
  private String categoryName;
  private Integer sortOrder;

  /** DB column active. JSON 키는 isActive. */
  @JsonProperty("isActive")
  private Boolean active;
}
