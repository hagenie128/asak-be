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
import com.asak.user.dto.menu.CategoryResponse;

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

    // 재료·옵션·영양·태그 write는 후순위 슬라이스
    // TODO-018: 메뉴 등록 BE 2/3 — 저장 서비스 구현.
    // 1) FileUtil.saveMenuImage(...) 결과 imageUrl 확정
    // 2) CreateMenuRequest(categoryId, name, price, description) + imageUrl 로 Mapper.insertMenu 호출
    // 3) 생성 menuId/요약 응답(menuId, categoryId, name, price, imageUrl, isSoldOut) 연결
    // 현재 범위는 categoryId, name, price, imageUrl, description 까지만 저장한다.
    // 예: /uploads/menu/UUID.png
  }

  // TODO-024: 메뉴 수정 BE 2/3 — 수정 서비스 구현.
  // 1) menuId 존재 여부/수정 건수 기준 정리
  // 2) UpdateMenuRequest 기본 필드(categoryId, name, price, imageUrl, description)로 Mapper.updateMenu 호출
  // 3) 성공 후 프론트가 상세/목록을 갱신할 수 있는 응답 규격과 맞춘다
  // TODO-030: 메뉴 삭제 BE 2/3 — 삭제 서비스 구현.
  // 1) soft delete/hard delete 정책 확정
  // 2) Mapper.deleteMenu 또는 대체 비활성화 호출
  // 3) 삭제 후 선택 메뉴/목록 refetch가 가능하도록 결과 반환 기준 정리

  public PageResult<MenuListResponse> getMenus(MenuListRequest request) {
    List<MenuListResponse> content = adminMenuMapper.getMenus(request);
    long totalElements = adminMenuMapper.countMenus(request);
    return new PageResult<>(content, request.getPage(), request.getSize(), totalElements);
  }

  public MenuDetailResponse getMenuDetail(Long menuId) {
    return adminMenuMapper.getMenuDetail(menuId);
  }

  public List<CategoryResponse> getCategories() {
    return adminMenuMapper.getCategories();
  }
}
