package com.asak.user.mapper;

import java.util.List;

import com.asak.user.dto.CategoryResponse;
import com.asak.user.dto.MenuListItemResponse;

public interface UserMenuMapper {

    List<CategoryResponse> selectCategory();

    List<MenuListItemResponse> selectMenuList();
}
