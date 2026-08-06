package com.asak.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 메뉴 등록 Request (POST /api/admin/menus).
 *
 * <p>계약 MVP 범위: 메뉴 기본 정보만. 재료·옵션·영양·알레르기·태그 write는 후순위 슬라이스.
 *
 * @see <a href="MENU_API_CONTRACT.md">Admin basic create/update</a>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

  private Long categoryId;
  private String name;
  private Integer price;
  private String imageUrl;
  private String description;
}
