package com.asak.admin.service;

import com.asak.admin.dto.request.item.SoldOutChangeRequest;
import com.asak.admin.dto.request.item.SoldOutPatchRequest;
import com.asak.admin.dto.response.item.SoldOutCatalogItemResponse;
import com.asak.admin.dto.response.item.SoldOutCatalogResponse;
import com.asak.admin.mapper.AdminSoldOutMapper;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO-008: 품절 2/4 — Service/Mapper/DTO 구현.
// 1) 카탈로그 조회(getSoldOutCatalog)와 PATCH 대상(patchSoldOut)을 추가하고 targetType별 대상 조회를 분리한다.
// 2) targetType/targetId/isSoldOut 검증, 중복 target 병합/거절, 없는 target·0건 update 규칙을 ErrorCode와 맞춘다.
// 3) changes가 여러 건이면 전체 롤백/부분 성공 중 하나를 정하고 transaction으로 보장한 뒤 TODO-007/009/010과 연결한다.
@Service
public class AdminSoldOutService {
  private final AdminSoldOutMapper adminSoldOutMapper;

  public AdminSoldOutService(AdminSoldOutMapper adminSoldOutMapper) {
    this.adminSoldOutMapper = adminSoldOutMapper;
  }

  public SoldOutCatalogResponse getSoldOutCatalog() {
    return splitCatalog(adminSoldOutMapper.getSoldOutCatalog());
  }

  @Transactional
  public SoldOutCatalogResponse patchSoldOut(SoldOutPatchRequest request) {
    List<SoldOutChangeRequest> changes = request == null ? null : request.getChanges();
    if (changes == null || changes.isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
    }

    Set<String> seen = new HashSet<>();
    for (SoldOutChangeRequest change : changes) {
      if (change == null
          || change.getTargetType() == null
          || change.getTargetId() == null
          || change.getTargetId() <= 0
          || change.getIsSoldOut() == null
          || !seen.add(change.getTargetType() + ":" + change.getTargetId())) {
        throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
      }
      if (update(change) != 1) {
        throw new CustomException(notFoundError(change.getTargetType()));
      }
    }
    return getSoldOutCatalog();
  }

  private int update(SoldOutChangeRequest change) {
    return switch (change.getTargetType()) {
      case "MENU" -> adminSoldOutMapper.updateMenuSoldOut(change.getTargetId(), change.getIsSoldOut());
      case "INGREDIENT" ->
          adminSoldOutMapper.updateIngredientSoldOut(change.getTargetId(), change.getIsSoldOut());
      case "OPTION_ITEM" ->
          adminSoldOutMapper.updateOptionItemSoldOut(change.getTargetId(), change.getIsSoldOut());
      default -> throw new CustomException(ErrorCode.INVALID_ORDER_REQUEST);
    };
  }

  private ErrorCode notFoundError(String targetType) {
    return switch (targetType) {
      case "MENU" -> ErrorCode.MENU_NOT_FOUND;
      case "INGREDIENT" -> ErrorCode.INGREDIENT_NOT_FOUND;
      default -> ErrorCode.INVALID_OPTION_SELECTION;
    };
  }

  private SoldOutCatalogResponse splitCatalog(List<SoldOutCatalogItemResponse> rows) {
    List<SoldOutCatalogItemResponse> available =
        rows.stream().filter(row -> !row.isSoldOut()).toList();
    List<SoldOutCatalogItemResponse> soldOut = rows.stream().filter(SoldOutCatalogItemResponse::isSoldOut).toList();
    return SoldOutCatalogResponse.builder().available(available).soldOut(soldOut).build();
  }
}
