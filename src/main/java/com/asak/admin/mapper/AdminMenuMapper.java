package com.asak.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;

public interface AdminMenuMapper {

  List<MenuListResponse> getMenus(@Param("request") MenuListRequest request);

  long countMenus(@Param("request") MenuListRequest request);

  MenuDetailResponse getMenuDetail(@Param("menuId") Long menuId);

  // TODO-016: insertMenu(...)
  // TODO-019: updateMenu(...)
  // TODO-026: deleteMenu(...) 또는 updateMenuActive
  // TODO-029: listIngredients(...) — 재료 검색
}
