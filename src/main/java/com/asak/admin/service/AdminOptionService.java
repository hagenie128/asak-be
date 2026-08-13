package com.asak.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.CreateMenuOptionGroupRequest;
import com.asak.admin.mapper.AdminOptionMapper;

import jakarta.transaction.Transactional;

@Service
public class AdminOptionService {
    private final AdminOptionMapper adminOptionMapper;

  public List<AdminOptionGroupResponse> getOptionGroups() { ... }

  public boolean existsOptionGroup(Long optionGroupId) { ... } // 기존 getOptionGroupDetail(boolean 용도)

    @Transactional
    public void replaceMenuOptionGroups(Long menuId, List<CreateMenuOptionGroupRequest> optionGroups) {
        adminOptionMapper.deleteMenuOptOverrides(menuId);
        adminOptionMapper.deleteMenuOptionGroups(menuId);
        insertOptionGroups(menuId, optionGroups); // 기존 로직 그대로 이동
    }

  void insertOptionGroups(Long menuId, List<CreateMenuOptionGroupRequest> optionGroups) { ... }

  private Long resolveRecommendedOptionItemId(...) { ... }

  private void insertRecommendedOverrides(...) { ... }
}