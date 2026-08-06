package com.asak.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.service.AdminMenuService;
import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.common.response.PageResult;
import com.asak.user.dto.menu.CategoryResponse;

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

  @GetMapping("/{menuId}")
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

  @GetMapping("/categories")
  public ApiResponse<PageResult<CategoryResponse>> getCategories() {
    return ApiResponse.success(
        "ADMIN_CATEGORY_LIST_SUCCESS",
        "관리자 카테고리 목록 조회 성공",
        new PageResult<>(adminMenuService.getCategories(), 0, 20, adminMenuService.getCategories().size()));
  }
  // TODO-019: POST /api/admin/menus 구현.
  // 1) Request 형식 결정: multipart(FormData)인지 JSON인지 확정
  // 2) CreateMenuRequest + imageFile을 받아 adminMenuService.createMenu 호출
  // 3) ADMIN_MENU_UPSERT_SUCCESS + 요약 응답(menuId, categoryId, name, price, imageUrl, isSoldOut) 반환
  // TODO-022: PATCH /api/admin/menus/{menuId} 구현.
  // 1) UpdateMenuRequest 바인딩
  // 2) adminMenuService.updateMenu(menuId, request) 호출
  // 3) 존재하지 않는 menuId는 MENU_NOT_FOUND 또는 팀 규칙 코드 반환
  // TODO-028: DELETE /api/admin/menus/{menuId} 구현.
  // 1) 삭제 정책(soft/hard) 확정
  // 2) adminMenuService.deleteMenu(menuId) 호출
  // 3) 목록 화면 refetch가 가능하도록 성공/실패 응답 규격 정리
  // TODO-029: GET /ingredients 또는 별도 IngredientsController 구현.
  // 1) endpoint 경로 결정
  // 2) 재료 검색/자동완성용 응답 shape 정의
  // 3) MenuEditPanel autocomplete/source에 연결
}
