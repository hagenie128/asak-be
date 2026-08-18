package com.asak.user.mapper;

import com.asak.user.dto.menu.CategoryResponse;
import com.asak.user.dto.menu.IngredientResponse;
import com.asak.user.dto.menu.MenuDetailResponse;
import com.asak.user.dto.menu.MenuListItemResponse;
import com.asak.user.dto.menu.OptionGroupResponse;
import com.asak.user.dto.menu.OptionItemResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserMenuMapper {

  // --카테고리---
  List<CategoryResponse> selectCategory();

  // --메뉴 리스트---
  List<MenuListItemResponse> selectMenuList();

  // --메뉴 디테일---
  MenuDetailResponse selectMenuDetail(Long menuId);

  List<IngredientResponse> selectIngredients(Long menuId);

  List<OptionGroupResponse> selectOptionGroups(Long menuId);

  List<OptionItemResponse> selectOptionItems(
      @Param("menuId") Long menuId, @Param("policyId") Long policyId);

  List<String> selectTages(@Param("menuId") Long menuId);
}
