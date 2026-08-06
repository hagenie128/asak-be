package com.asak.admin.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.admin.mapper.AdminMenuMapper;
import com.asak.common.response.PageResult;
import com.asak.common.util.FileUtil;

@Service
public class AdminMenuService {

  private final AdminMenuMapper adminMenuMapper;

  public AdminMenuService(AdminMenuMapper adminMenuMapper) {
    this.adminMenuMapper = adminMenuMapper;
  }

  @Value("${app.file.menu-upload-dir}")
  private String menuUploadDir;

  public void createMenu(MultipartFile imageFile) throws IOException {
    String imageUrl = FileUtil.saveMenuImage(
        imageFile,
        Paths.get(menuUploadDir));

    // TODO-015: CreateMenuRequest 필드 채우기 (name, price, category, ingredients, optionGroups…)
    // TODO-017: imageUrl + request 로 menu INSERT + ingredients/optionGroups 트랜잭션 저장
    // 예: /uploads/menu/UUID.png
  }

  // TODO-020: updateMenu(menuId, UpdateMenuRequest) — Mapper updateMenu 호출
  // TODO-027: deleteMenu(menuId) — soft delete 또는 isActive=false

  public PageResult<MenuListResponse> getMenus(MenuListRequest request) {
    List<MenuListResponse> content = adminMenuMapper.getMenus(request);
    long totalElements = adminMenuMapper.countMenus(request);
    return new PageResult<>(content, request.getPage(), request.getSize(), totalElements);
  }

  public MenuDetailResponse getMenuDetail(Long menuId) {
    return adminMenuMapper.getMenuDetail(menuId);
  }
}
