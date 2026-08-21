package com.asak.admin.mapper;

import com.asak.admin.dto.response.item.SoldOutCatalogItemResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;

// TODO-008 (구현 완료): vw_soldout_catalog가 공통 카탈로그 필드와 image_url을 모두 제공한다.
// PATCH는 targetType별 UPDATE를 분리하며, 변경 건수(0/1)는 Service가 대상 검증과 전체 트랜잭션에 사용한다.
// QA: 배포 DB View 존재 여부, View의 image_url 값, 화면의 이미지 fallback을 확인한다.
public interface AdminSoldOutMapper {
  List<SoldOutCatalogItemResponse> getSoldOutCatalog();

  int updateMenuSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);

  int updateIngredientSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);

  int updateOptionItemSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);
}
