package com.asak.admin.dto.request.menus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 메뉴 수정 Request (PATCH /api/admin/menus/{menuId}).
 *
 * <p>본문 필드는 Create와 동일. menuId는 path variable.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuRequest {

  private Long categoryId;
  private String name;
  private Integer price;
  private Long mediaAssetId;
  private String imageUrl;
  private String description;
}
