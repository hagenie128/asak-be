package com.asak.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.CreateMenuRequest;
import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.AdminCategoryResponse;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.dto.response.IngredientResponse;

public interface AdminMenuMapper {

  List<MenuListResponse> getMenus(@Param("request") MenuListRequest request);

  long countMenus(@Param("request") MenuListRequest request);

  MenuDetailResponse getMenuDetail(@Param("menuId") Long menuId);

  List<AdminCategoryResponse> getCategories();

  int insertMenu(Map<String, Object> map);

  Long findCommonCodeId(@Param("groupCode") String groupCode, @Param("code") String code);

  Long findTagId(@Param("code") String code, @Param("name") String name);

  Long findActiveMediaAssetId(@Param("mediaAssetId") Long mediaAssetId);

  Long findActiveMediaAssetIdByUrl(@Param("url") String url);

  Long findOptPolicyId(@Param("optionGroupId") Long optionGroupId);

  List<Long> findOptItemIdsByPolicyId(@Param("policyId") Long policyId);

  int insertMenuIngredient(Map<String, Object> map);

  int insertMenuOptPolicy(Map<String, Object> map);

  int upsertMenuOptOverride(Map<String, Object> map);

  int insertMenuNutrition(Map<String, Object> map);

  int insertMenuTag(Map<String, Object> map);

  int updateMenu(Map<String, Object> map);

  /** Soft delete: deleted_at 설정 (주문 FK 유지) */
  int softDeleteMenu(@Param("menuId") Long menuId);

  int deleteMenuIngredients(Long menuId);

  int deleteMenuOptionGroups(Long menuId);

  int deleteMenuOptOverrides(Long menuId);

  int deleteMenuNutrition(Long menuId);

  int deleteMenuTags(Long menuId);

  List<IngredientResponse> getIngredients();
}
