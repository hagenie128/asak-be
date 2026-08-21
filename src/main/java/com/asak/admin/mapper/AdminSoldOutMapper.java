package com.asak.admin.mapper;

import com.asak.admin.dto.response.item.SoldOutCatalogItemResponse;
import java.util.List;
import org.apache.ibatis.annotations.Param;

// 품절 구현 참고: 카탈로그 SELECT.
// 1) MENU / INGREDIENT / OPTION_ITEM 대상별로 어떤 테이블/뷰에서 읽을지 확정
// 2) 화면이 쓰는 공통 row shape(targetType, targetId, name, isSoldOut ...)로 맞춘다
// 3) SoldOutManagePage/useSoldOutDraft 가 바로 쓸 수 있게 XML SELECT 추가
// 품절 구현 참고: is_sold_out UPDATE.
// 1) targetType + targetId 별 분기 방식 결정(case/동적 SQL/쿼리 분리)
// 2) true/false 토글 UPDATE 추가
// 3) 변경 건수(0/1)를 Service/Controller가 검증에 쓸 수 있게 반환
public interface AdminSoldOutMapper {
  List<SoldOutCatalogItemResponse> getSoldOutCatalog();

  int updateMenuSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);

  int updateIngredientSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);

  int updateOptionItemSoldOut(@Param("targetId") Long targetId, @Param("isSoldOut") boolean isSoldOut);
}
