package com.asak.admin.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.dto.request.CreateMenuRequest;
import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.AdminCategoryResponse;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.service.AdminMenuService;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.common.response.PageResult;
import com.asak.admin.dto.response.IngredientResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

  private final AdminMenuService adminMenuService;

  public AdminMenuController(AdminMenuService adminMenuService) {
    this.adminMenuService = adminMenuService;
  }

  // {{baseUrl}}/api/admin/menus?categoryId={{categoryId}}&keyword=&isSoldOut=false&tagId=&page=0&size=20&sort=name,asc
  @GetMapping
  public ApiResponse<PageResult<MenuListResponse>> getMenus(
      @Valid @ModelAttribute MenuListRequest request) {
    PageResult<MenuListResponse> menus = adminMenuService.getMenus(request);
    return ApiResponse.success(
        "ADMIN_MENU_LIST_SUCCESS",
        "관리자 메뉴 목록 조회 성공",
        menus);
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
    return ApiResponse.success(
        "ADMIN_MENU_INGREDIENTS_SUCCESS",
        "관리자 재료 목록 조회 성공",
        ingredients);
  }

  @GetMapping("/{menuId:\\d+}")
  public ApiResponse<MenuDetailResponse> getMenuDetail(@PathVariable Long menuId) {
    MenuDetailResponse menuDetail = adminMenuService.getMenuDetail(menuId);
    if (menuDetail == null) {
      return ApiResponse.error(
          ErrorCode.MENU_NOT_FOUND);
    }
    return ApiResponse.success(
        "ADMIN_MENU_DETAIL_SUCCESS",
        "관리자 메뉴 상세 조회 성공",
        menuDetail);
  }

  // TODO-017: POST /api/admin/menus — JSON body(CreateMenuRequest). 이미지 파일 업로드는
  // 후순위.
  @PostMapping
  public ApiResponse<MenuDetailResponse> createMenu(@Valid @RequestBody CreateMenuRequest request) {
    if (request.getCategoryId() == null || request.getCategoryId() <= 0) {
      return ApiResponse.error(
          ErrorCode.MENU_CREATE_INVALID);
    }
    MenuDetailResponse menu = adminMenuService.createMenu(request);
    return ApiResponse.success(
        "ADMIN_MENU_UPSERT_SUCCESS",
        "관리자 메뉴 등록 성공",
        menu);
  }

  @PatchMapping("/{menuId:\\d+}")
  public ApiResponse<MenuDetailResponse> updateMenu(@PathVariable Long menuId,
      @Valid @RequestBody CreateMenuRequest request) {
    if (menuId == null || menuId <= 0) {
      return ApiResponse.error(ErrorCode.MENU_NOT_FOUND);
    }
    try {
      MenuDetailResponse updated = adminMenuService.updateMenu(menuId, request);
      return ApiResponse.success(
          "ADMIN_MENU_UPDATE_SUCCESS",
          "관리자 메뉴 수정 성공",
          updated);
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
      return ApiResponse.success(
          "ADMIN_MENU_DELETE_SUCCESS",
          "관리자 메뉴 삭제 성공",
          null);
    } catch (CustomException e) {
      return ApiResponse.error(e.getErrorCode());
    }
  }
}
