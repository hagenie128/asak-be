package com.asak.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.AdminCategoryResponse;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;

public interface AdminMenuMapper {

  List<MenuListResponse> getMenus(@Param("request") MenuListRequest request);

  long countMenus(@Param("request") MenuListRequest request);

  MenuDetailResponse getMenuDetail(@Param("menuId") Long menuId);

  List<AdminCategoryResponse> getCategories();

  int insertMenu(Map<String, Object> map);

  Long findCommonCodeId(@Param("groupCode") String groupCode, @Param("code") String code);

  Long findTagId(@Param("code") String code, @Param("name") String name);

  Long findOptPolicyId(@Param("optionGroupId") Long optionGroupId);

  List<Long> findOptItemIdsByPolicyId(@Param("policyId") Long policyId);

  int insertMenuIngredient(Map<String, Object> map);

  int insertMenuOptPolicy(Map<String, Object> map);

  int upsertMenuOptOverride(Map<String, Object> map);

  int insertMenuNutrition(Map<String, Object> map);

  int insertMenuTag(Map<String, Object> map);

  // TODO-025: 메뉴 수정 BE 3/3 — PATCH 저장용 UPDATE Mapper 추가.
  // TODO-031: 메뉴 삭제 BE 3/3 — DELETE 정책용 Mapper 추가.
  // TODO-036: 재료 검색 BE 2/2 — 메뉴 편집 화면 재료 검색용 Mapper 추가.
}
