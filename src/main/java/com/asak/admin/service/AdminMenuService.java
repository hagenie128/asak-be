package com.asak.admin.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.asak.admin.dto.request.MenuListRequest;
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

    // imageUrl 값을 menu.image_url 컬럼에 저장
    // 예: /uploads/menu/UUID.png
  }

  public PageResult<MenuListResponse> getMenus(MenuListRequest request) {
    // 메뉴 목록 조회 로직 구현
    List<MenuListResponse> content = adminMenuMapper.getMenus(request);
    long totalElements = adminMenuMapper.countMenus(request);
    return new PageResult<>(content, request.getPage(), request.getSize(), totalElements);
  }
}
