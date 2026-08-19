package com.asak.admin.controller;

import com.asak.admin.dto.request.CreateMenuRequest;
import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.AdminCategoryResponse;
import com.asak.admin.dto.response.IngredientResponse;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.service.AdminMenuService;
import com.asak.admin.service.AdminOptionService;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.common.response.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

  private final AdminMenuService adminMenuService;
  private final AdminOptionService adminOptionService;

  public AdminMenuController(
      AdminMenuService adminMenuService, AdminOptionService adminOptionService) {
    this.adminMenuService = adminMenuService;
    this.adminOptionService = adminOptionService;
  }

  // {{baseUrl}}/api/admin/menus?categoryId={{categoryId}}&keyword=&isSoldOut=false&tagId=&page=0&size=20&sort=name,asc
  @GetMapping
  public ApiResponse<PageResult<MenuListResponse>> getMenus(
      @Valid @ModelAttribute MenuListRequest request) {
    PageResult<MenuListResponse> menus = adminMenuService.getMenus(request);
    return ApiResponse.success("ADMIN_MENU_LIST_SUCCESS", "관리자 메뉴 목록 조회 성공", menus);
  }

  // 고정 경로는 /{menuId}보다 위에 둔다. (Spring이 "ingredients"를 menuId로 잡는 것 방지)
  @GetMapping("/categories")
  public ApiResponse<PageResult<AdminCategoryResponse>> getCategories() {
    var categories = adminMenuService.getCategories();
    int size = categories.size();
    return ApiResponse.success(
        "ADMIN_CATEGORY_LIST_SUCCESS",
        "관리자 카테고리 목록 조회 성공",
        new PageResult<>(categories, 0, Math.max(size, 1), size));
  }

  @GetMapping("/ingredients")
  public ApiResponse<PageResult<IngredientResponse>> getIngredients() {
    PageResult<IngredientResponse> ingredients = adminMenuService.getIngredients();
    return ApiResponse.success("ADMIN_MENU_INGREDIENTS_SUCCESS", "관리자 재료 목록 조회 성공", ingredients);
  }

  @GetMapping("/{menuId:\\d+}")
  public ApiResponse<MenuDetailResponse> getMenuDetail(@PathVariable Long menuId) {
    MenuDetailResponse menuDetail = adminMenuService.getMenuDetail(menuId);
    if (menuDetail == null) {
      return ApiResponse.error(ErrorCode.MENU_NOT_FOUND);
    }
    return ApiResponse.success("ADMIN_MENU_DETAIL_SUCCESS", "관리자 메뉴 상세 조회 성공", menuDetail);
  }

  // TODO-003: POST /api/admin/menus — JSON body(CreateMenuRequest)만 처리한다. 이미지 파일
  // 업로드는 후순위다.
  // 저장 전 categoryId·optionGroupIds·ingredientIds의 실제 존재/활성 여부를 Service에서 검증하고,
  // 생성 성공 뒤 GET 상세와 목록에 같은 menuId가 보이는지 API·DB 기준으로 확인한다.
  @PostMapping
  public ApiResponse<MenuDetailResponse> createMenu(@Valid @RequestBody CreateMenuRequest request) {
    if (request.getCategoryId() != null && request.getCategoryId() > 0) {
      if (!adminMenuService.getCategoryById(request.getCategoryId())) {
        return ApiResponse.error(ErrorCode.CATEGORY_NOT_FOUND);
      }
    }
    if (request.getOptionGroups() != null && request.getOptionGroups().size() > 0) {
      for (var group : request.getOptionGroups()) {
        if (group.getOptionGroupId() == null
            || !adminOptionService.existsOptionGroup(group.getOptionGroupId())) {
          return ApiResponse.error(ErrorCode.MENU_OPTION_GROUP_NOT_FOUND);
        }
      }
    }
    if (request.getIngredients() != null && request.getIngredients().size() > 0) {
      for (var ingredient : request.getIngredients()) {
        if (ingredient.getIngredientId() == null
            || !adminMenuService.getIngredientDetail(ingredient.getIngredientId())) {
          return ApiResponse.error(ErrorCode.MENU_INGREDIENT_NOT_FOUND);
        }
      }
    }
    MenuDetailResponse menu = adminMenuService.createMenu(request);
    return ApiResponse.success("ADMIN_MENU_UPSERT_SUCCESS", "관리자 메뉴 등록 성공", menu);
  }

  @PatchMapping("/{menuId:\\d+}")
  public ApiResponse<MenuDetailResponse> updateMenu(
      @PathVariable Long menuId, @Valid @RequestBody CreateMenuRequest request) {
    if (menuId == null || menuId <= 0) {
      return ApiResponse.error(ErrorCode.MENU_NOT_FOUND);
    }
    try {
      MenuDetailResponse updated = adminMenuService.updateMenu(menuId, request);
      return ApiResponse.success("ADMIN_MENU_UPDATE_SUCCESS", "관리자 메뉴 수정 성공", updated);
    } catch (CustomException e) {
      return ApiResponse.error(e.getErrorCode());
    } catch (Exception e) {
      return ApiResponse.error(ErrorCode.MENU_UPDATE_FAILED);
    }
  }

  /** DELETE /api/admin/menus/{menuId} — soft delete (deleted_at). 주문 이력 유지 */
  @DeleteMapping("/{menuId:\\d+}")
  public ApiResponse<Void> deleteMenu(@PathVariable Long menuId) {
    if (menuId == null || menuId <= 0) {
      return ApiResponse.error(ErrorCode.MENU_DELETE_INVALID);
    }
    try {
      adminMenuService.deleteMenu(menuId);
      return ApiResponse.success("ADMIN_MENU_DELETE_SUCCESS", "관리자 메뉴 삭제 성공", null);
    } catch (CustomException e) {
      return ApiResponse.error(e.getErrorCode());
    }
  }
}
