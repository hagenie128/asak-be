package com.asak.admin.controller;

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
    if (menus.getContent().isEmpty()) {
      return ApiResponse.error(
          ErrorCode.MENU_NOT_FOUND);
    }
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

}
