package com.asak.admin.mapper;

import com.asak.admin.dto.response.item.OptionGroupSummaryResponse;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AdminOptionMapper {

  List<OptionGroupSummaryResponse> getOptionGroups();

  OptionGroupSummaryResponse getOptionGroupDetail(@Param("optionGroupId") Long optionGroupId);

  Long findOptPolicyId(@Param("optionGroupId") Long optionGroupId);

  List<Long> findOptItemIdsByPolicyId(@Param("policyId") Long policyId);

  int insertMenuOptPolicy(Map<String, Object> map);

  int upsertMenuOptOverride(Map<String, Object> map);

  int deleteMenuOptOverrides(@Param("menuId") Long menuId);

  int deleteMenuOptionGroups(@Param("menuId") Long menuId);
}
