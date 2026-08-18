package com.asak.admin.controller;

import com.asak.admin.dto.response.OptionGroupSummaryResponse;
import com.asak.admin.service.AdminOptionService;
import com.asak.common.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/opts")
public class AdminOptionController {
  private final AdminOptionService adminOptionService;

  public AdminOptionController(AdminOptionService adminOptionService) {
    this.adminOptionService = adminOptionService;
  }

  @GetMapping("/groups")
  public ApiResponse<List<OptionGroupSummaryResponse>> getOptionGroups() {
    List<OptionGroupSummaryResponse> optionGroups = adminOptionService.getOptionGroups();
    return ApiResponse.success(
        "ADMIN_OPTION_GROUP_LIST_SUCCESS", "관리자 옵션 그룹 목록 조회 성공", optionGroups);
  }

  @GetMapping("/{optionGroupId}")
  public ApiResponse<OptionGroupSummaryResponse> getOptionGroupDetail(
      @PathVariable Long optionGroupId) {
    OptionGroupSummaryResponse optionGroup = adminOptionService.getOptionGroupDetail(optionGroupId);
    return ApiResponse.success(
        "ADMIN_OPTION_GROUP_DETAIL_SUCCESS", "관리자 옵션 그룹 상세 조회 성공", optionGroup);
  }
}
