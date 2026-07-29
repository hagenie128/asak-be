package com.asak.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.user.dto.MenuDetailResponse;
import com.asak.user.dto.CategoryResponse;
import com.asak.user.dto.IngredientResponse;
import com.asak.user.dto.MenuListItemResponse;
import com.asak.user.dto.Nutrition;
import com.asak.user.dto.OptionGroupResponse;
import com.asak.user.dto.OptionItemResponse;

public interface UserMenuMapper {

    // --카테고리---
    List<CategoryResponse> selectCategory();

    // --메뉴 리스트---
    List<MenuListItemResponse> selectMenuList();

    // --메뉴 디테일---
    MenuDetailResponse selectMenuDetail(Long menuId);

    List<IngredientResponse> selectIngredients(Long menuId);

    List<OptionGroupResponse> selectOptionGroups(Long menuId);

    List<OptionItemResponse> selectOptionItems(@Param("menuId") Long menuId, @Param("policyId") Long policyId);

    Nutrition selectNutrition(Long menuId);

}
