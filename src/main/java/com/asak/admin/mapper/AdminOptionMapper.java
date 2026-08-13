package com.asak.admin.mapper;

import java.util.List;
import java.util.Map;

public interface AdminOptionMapper {
    List<AdminOptionGroupResponse> getOptionGroups(); // 신규: 목록 조회

    AdminOptionGroupResponse getOptionGroupDetail(Long optionGroupId); // 기존 existence-check(Object)를 DTO로 승격

    Long findOptPolicyId(Long optionGroupId);

    List<Long> findOptItemIdsByPolicyId(Long policyId);

    int insertMenuOptPolicy(Map<String, Object> map);

    int upsertMenuOptOverride(Map<String, Object> map);

    int deleteMenuOptOverrides(Long menuId);

    int deleteMenuOptionGroups(Long menuId);
}