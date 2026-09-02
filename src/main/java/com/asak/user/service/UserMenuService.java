package com.asak.user.service;

import com.asak.user.dto.menu.CategoryResponse;
import com.asak.user.dto.menu.MenuDetailResponse;
import com.asak.user.dto.menu.MenuListItemResponse;
import com.asak.user.dto.menu.MenuTagRow;
import com.asak.user.dto.menu.OptionGroupResponse;
import com.asak.user.mapper.UserMenuMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    List<MenuListItemResponse> menus = menuMapper.selectMenuList();
    if (menus.isEmpty()) {
      return menus;
    }

    Map<Long, List<String>> tagsByMenuId = new HashMap<>();
    for (MenuTagRow row : menuMapper.selectAllMenuTags()) {
      tagsByMenuId
          .computeIfAbsent(row.getMenuId(), ignored -> new ArrayList<>())
          .add(row.getTagName());
    }

    for (MenuListItemResponse menu : menus) {
      menu.setTags(tagsByMenuId.getOrDefault(menu.getMenuId(), List.of()));
    }

    return menus;
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
