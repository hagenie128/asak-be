package com.asak.user.service;

import com.asak.user.dto.menu.CategoryResponse;
import com.asak.user.dto.menu.MenuDetailResponse;
import com.asak.user.dto.menu.MenuListItemResponse;
import com.asak.user.dto.menu.OptionGroupResponse;
import com.asak.user.mapper.UserMenuMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    if (response == null) {
      return null;
    }

    response.setIngredients(menuMapper.selectIngredients(menuId));

    List<OptionGroupResponse> groups = menuMapper.selectOptionGroups(menuId);

    // group 담을 그릇 , groups반복할 객체
    for (OptionGroupResponse group : groups) {
      group.setItems(menuMapper.selectOptionItems(menuId, group.getOptionGroupId()));
    }

    response.setOptionGroups(groups);

    response.setTags(menuMapper.selectTages(menuId));

    return response;
  }
}
