package com.asak.admin.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 메뉴 등록 Request (POST /api/admin/menus).
 *
 * <p>기본 정보 + 관련 테이블(menu_ing, menu_opt_policy, menu_nutr, menu_tag) write.
 * allergens 는 재료의 ing_allergen 조인으로 상세 조회 시 자동 조립된다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

  private Long categoryId;
  private String name;
  private Integer price;
  /** menu.image_asset_id. 새 클라이언트는 이 값을 우선 전송한다. */
  private Long mediaAssetId;
  /**
   * 이전 관리자 화면과의 호환용 URL. 서비스에서 활성 media_asset을 조회해 mediaAssetId로 변환한다.
   */
  private String imageUrl;
  private String description;
  private List<CreateMenuIngredientRequest> ingredients;
  private List<CreateMenuOptionGroupRequest> optionGroups;
  private CreateMenuNutritionRequest nutrition;
  private List<CreateMenuTagRequest> tags;
}
