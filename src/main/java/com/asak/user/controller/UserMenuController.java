package com.asak.user.controller;

import com.asak.common.exception.ErrorCode;
import com.asak.common.response.ApiResponse;
import com.asak.user.dto.menu.CategoryResponse;
import com.asak.user.dto.menu.MenuDetailResponse;
import com.asak.user.dto.menu.MenuListItemResponse;
import com.asak.user.dto.menu.MenuListResponse;
import com.asak.user.service.UserMenuService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * RestController 에서는 요청별 이노테이션을 적용시켜줘야함
 *
 * GetMapping -> 조회
 * PostMapping -> 추가
 * put -> 데이터 전체 수정
 * patch -> 데이터 부분 수정
 * delete -> 삭제
 *
 * */

// ---[메뉴]---
// 카테고리 조회
// 메뉴 목록 조회
// 메뉴 상세 조회

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserMenuController {

  // 메뉴 서비스 연결
  private final UserMenuService menuService;

  // 카테고리 조회
  @GetMapping("/categories")
  public ApiResponse<List<CategoryResponse>> getCategories() {

    List<CategoryResponse> categories = menuService.selectCategory();

    return ApiResponse.success(categories);
  }

  // 메뉴 목록 조회
  @GetMapping("/menuList")
  public ApiResponse<MenuListResponse> getMenuList() {

    List<CategoryResponse> categories = menuService.selectCategory();
    List<MenuListItemResponse> menus = menuService.selectMenuList();

    MenuListResponse menuList = new MenuListResponse();
    menuList.setCategories(categories);
    menuList.setMenus(menus);

    return ApiResponse.success(menuList);
  }

  // 메뉴 디테일 목록 조회
  @GetMapping("menuDetail/{menuId}")
  public ApiResponse<MenuDetailResponse> getMenuDetail(@PathVariable("menuId") Long menuId) {

    MenuDetailResponse menuDetailResponse = menuService.selectMenuDetail(menuId);
    if (menuDetailResponse == null) {
      return ApiResponse.error(ErrorCode.MENU_NOT_FOUND);
    }

    return ApiResponse.success(menuDetailResponse);
  }
}
