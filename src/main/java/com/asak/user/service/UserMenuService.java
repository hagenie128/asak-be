package com.asak.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.user.dto.MenuDetailResponse;
import com.asak.user.dto.CategoryResponse;
import com.asak.user.dto.MenuListItemResponse;
import com.asak.user.dto.OptionGroupResponse;
import com.asak.user.dto.OptionItemResponse;
import com.asak.user.mapper.UserMenuMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final UserMenuMapper menuMapper;

    public List<CategoryResponse> selectCategory() {
        return menuMapper.selectCategory();
    }

    public List<MenuListItemResponse> selectMenuList() {
        return menuMapper.selectMenuList();
    }

    // 메뉴디테일
    public MenuDetailResponse selectMenuDetail(Long menuId) {

        MenuDetailResponse response = menuMapper.selectMenuDetail(menuId);

        response.setIngredients(menuMapper.selectIngredients(menuId));

        List<OptionGroupResponse> groups = menuMapper.selectOptionGroups(menuId);

        // group 담을 그릇 , groups반복할 객체
        for (OptionGroupResponse group : groups) {
            group.setItems(
                    menuMapper.selectOptionItems(menuId,group.getOptionGroupId()));
        }

        response.setOptionGroups(groups);

        response.setNutrition(menuMapper.selectNutrition(menuId));

        return response;
    }
}
