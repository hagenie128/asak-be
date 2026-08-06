package com.asak.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.MenuListRequest;
import com.asak.admin.dto.response.MenuDetailResponse;
import com.asak.admin.dto.response.MenuListResponse;
import com.asak.user.dto.menu.CategoryResponse;

public interface AdminMenuMapper {

  List<MenuListResponse> getMenus(@Param("request") MenuListRequest request);

  long countMenus(@Param("request") MenuListRequest request);

  MenuDetailResponse getMenuDetail(@Param("menuId") Long menuId);

  List<CategoryResponse> getCategories();

  // TODO-017: POST 저장용 INSERT Mapper 추가.
  // 1) AdminMenuMapper.xml에 insert id="insertMenu" 추가
  // 2) categoryId, name, price, imageUrl, description 를 menu 테이블에 저장
  // 3) 가능하면 generated key(menuId)까지 받아 Service/Controller 응답에 넘긴다
  // 함께 볼 파일: AdminMenuService.createMenu, AdminMenuController POST, AdminMenuMapper.xml.
  // TODO-020: PATCH 저장용 UPDATE Mapper 추가.
  // 1) AdminMenuMapper.xml에 update id="updateMenu" 추가
  // 2) UpdateMenuRequest(categoryId, name, price, imageUrl, description) 기본 필드만 수정
  // 3) 수정 건수(0/1)를 Service가 존재 여부 판단에 쓸 수 있게 반환
  // 함께 볼 파일: AdminMenuService.updateMenu, AdminMenuController PATCH, AdminMenuMapper.xml.
  // TODO-026: DELETE 정책용 Mapper 추가.
  // 1) soft delete 가능 여부 확인
  // 2) 가능하면 updateMenuActive, 아니면 hard delete SQL 추가
  // 3) 삭제 건수(0/1)를 Service/Controller가 후속 처리에 쓸 수 있게 반환
  // 함께 볼 파일: AdminMenuService.deleteMenu, AdminMenuController DELETE, AdminMenuMapper.xml.
  // TODO-029: 메뉴 편집 화면 재료 검색용 Mapper 추가.
  // 1) 재료 목록 SELECT 추가
  // 2) 검색어/페이지 필요 여부 결정
  // 3) GET /ingredients 또는 별도 endpoint에서 MenuEditPanel autocomplete/source로 사용
}
