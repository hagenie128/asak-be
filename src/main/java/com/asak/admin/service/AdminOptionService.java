package com.asak.admin.service;

import com.asak.admin.dto.request.CreateMenuOptionGroupRequest;
import com.asak.admin.dto.request.CreateMenuOptionItemRequest;
import com.asak.admin.dto.response.OptionGroupSummaryResponse;
import com.asak.admin.mapper.AdminOptionMapper;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOptionService {

  private final AdminOptionMapper adminOptionMapper;

  public AdminOptionService(AdminOptionMapper adminOptionMapper) {
    this.adminOptionMapper = adminOptionMapper;
  }

  public List<OptionGroupSummaryResponse> getOptionGroups() {
    return adminOptionMapper.getOptionGroups();
  }

  public OptionGroupSummaryResponse getOptionGroupDetail(Long optionGroupId) {
    if (optionGroupId == null || optionGroupId <= 0) {
      throw new CustomException(ErrorCode.MENU_OPTION_GROUP_NOT_FOUND);
    }
    OptionGroupSummaryResponse detail = adminOptionMapper.getOptionGroupDetail(optionGroupId);
    if (detail == null) {
      throw new CustomException(ErrorCode.MENU_OPTION_GROUP_NOT_FOUND);
    }
    return detail;
  }

  /** 메뉴 생성/수정 검증용. opt_group 또는 opt_policy id 모두 허용. */
  public boolean existsOptionGroup(Long optionGroupId) {
    if (optionGroupId == null || optionGroupId <= 0) {
      return false;
    }
    return adminOptionMapper.getOptionGroupDetail(optionGroupId) != null
        || adminOptionMapper.findOptPolicyId(optionGroupId) != null;
  }

  @Transactional
  public void replaceMenuOptionGroups(
      Long menuId, List<CreateMenuOptionGroupRequest> optionGroups) {
    adminOptionMapper.deleteMenuOptOverrides(menuId);
    adminOptionMapper.deleteMenuOptionGroups(menuId);
    insertOptionGroups(menuId, optionGroups);
  }

  public void insertOptionGroups(Long menuId, List<CreateMenuOptionGroupRequest> optionGroups) {
    if (optionGroups == null || optionGroups.isEmpty()) {
      return;
    }

    int sortNo = 1;
    for (CreateMenuOptionGroupRequest group : optionGroups) {
      if (group.getOptionGroupId() == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }
      Long policyId = adminOptionMapper.findOptPolicyId(group.getOptionGroupId());
      if (policyId == null) {
        throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
      }

      Map<String, Object> row = new HashMap<>();
      row.put("menuId", menuId);
      row.put("policyId", policyId);
      row.put("sortNo", sortNo++);
      row.put("required", Boolean.TRUE.equals(group.getIsRequired()) ? 1 : 0);
      adminOptionMapper.insertMenuOptPolicy(row);

      Long recommendedOptionItemId = resolveRecommendedOptionItemId(group);
      if (recommendedOptionItemId != null) {
        insertRecommendedOverrides(menuId, policyId, recommendedOptionItemId);
      }
    }
  }

  private Long resolveRecommendedOptionItemId(CreateMenuOptionGroupRequest group) {
    if (group.getRecommendedOptionItemId() != null) {
      return group.getRecommendedOptionItemId();
    }
    if (group.getItems() == null) {
      return null;
    }
    return group.getItems().stream()
        .filter(item -> Boolean.TRUE.equals(item.getIsRecommended()))
        .map(CreateMenuOptionItemRequest::getOptionItemId)
        .filter(id -> id != null)
        .findFirst()
        .orElse(null);
  }

  private void insertRecommendedOverrides(
      Long menuId, Long policyId, Long recommendedOptionItemId) {
    List<Long> optionItemIds = adminOptionMapper.findOptItemIdsByPolicyId(policyId);
    if (optionItemIds == null || optionItemIds.isEmpty()) {
      throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
    }
    if (!optionItemIds.contains(recommendedOptionItemId)) {
      throw new CustomException(ErrorCode.MENU_CREATE_INVALID);
    }

    for (Long optionItemId : optionItemIds) {
      Map<String, Object> override = new HashMap<>();
      override.put("menuId", menuId);
      override.put("optionItemId", optionItemId);
      override.put("recommended", optionItemId.equals(recommendedOptionItemId) ? 1 : 0);
      adminOptionMapper.upsertMenuOptOverride(override);
    }
  }
}
