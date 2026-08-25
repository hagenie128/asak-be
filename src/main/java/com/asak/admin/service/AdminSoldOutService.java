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

// vw_soldout_catalog로 카탈로그를 조회하고 MENU/INGREDIENT/OPTION_ITEM의 sold_out을 저장한다.
// targetType/targetId를 검증하고 여러 changes는 @Transactional 전체 롤백으로 처리한다.
// QA: 실제 DB 저장·복구, 없는 대상이 섞인 복수 변경의 전체 롤백, View와 이미지 보완 조인을 확인한다.
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
      case "MENU" ->
          adminSoldOutMapper.updateMenuSoldOut(change.getTargetId(), change.getIsSoldOut());
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
    List<SoldOutCatalogItemResponse> soldOut =
        rows.stream().filter(SoldOutCatalogItemResponse::isSoldOut).toList();
    return SoldOutCatalogResponse.builder().available(available).soldOut(soldOut).build();
  }
}
