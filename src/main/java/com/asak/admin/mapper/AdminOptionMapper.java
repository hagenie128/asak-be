package com.asak.admin.mapper;

import com.asak.admin.dto.response.item.OptionGroupSummaryResponse;
import com.asak.admin.dto.response.item.OptionItemSummaryResponse;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AdminOptionMapper {

  List<OptionGroupSummaryResponse> getOptionGroups();

  OptionGroupSummaryResponse getOptionGroupDetail(@Param("optionGroupId") Long optionGroupId);

  Long findOptPolicyId(@Param("optionGroupId") Long optionGroupId);

  List<Long> findOptItemIdsByPolicyId(@Param("policyId") Long policyId);

  List<OptionItemSummaryResponse> findOptionItemsByGroupId(
      @Param("optionGroupId") Long optionGroupId);

  List<OptionItemSummaryResponse> findMenuOptionItems(
      @Param("menuId") Long menuId, @Param("optionGroupId") Long optionGroupId);

  int insertMenuOptPolicy(Map<String, Object> map);

  int upsertMenuOptOverride(Map<String, Object> map);

  int deleteMenuOptOverrides(@Param("menuId") Long menuId);

  int deleteMenuOptionGroups(@Param("menuId") Long menuId);
}
