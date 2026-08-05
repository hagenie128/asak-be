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

  // TODO-033: insertMenu(...)
  // TODO-036: updateMenu(...)
  // TODO-039: deleteMenu(...) 또는 updateMenuActive
  // TODO-042: listIngredients(...) — 재료 검색
}
