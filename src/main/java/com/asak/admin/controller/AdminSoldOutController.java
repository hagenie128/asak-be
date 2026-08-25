package com.asak.admin.controller;

import com.asak.admin.dto.request.item.SoldOutPatchRequest;
import com.asak.admin.dto.response.item.SoldOutCatalogResponse;
import com.asak.admin.service.AdminSoldOutService;
import com.asak.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// GET 카탈로그와 PATCH changes[{targetType,targetId,isSoldOut}] 계약을 제공한다.
// Service가 대상 검증과 전체 트랜잭션을 맡고, Controller는 ApiResponse 성공·오류 규격을 프런트 API와 draft 훅에 연결한다.
// QA: 빈 changes, 중복·없는 대상, 메뉴·재료 혼합 변경의 Bruno 응답과 동시 변경 정책을 확인한다.
@RestController
@RequestMapping("/api/admin/soldOut")
public class AdminSoldOutController {
  private final AdminSoldOutService adminSoldOutService;

  public AdminSoldOutController(AdminSoldOutService adminSoldOutService) {
    this.adminSoldOutService = adminSoldOutService;
  }

  @GetMapping
  public ApiResponse<SoldOutCatalogResponse> getSoldOutCatalog() {
    return ApiResponse.success(
        "ADMIN_SOLD_OUT_CATALOG_SUCCESS", "품절 카탈로그 조회 성공", adminSoldOutService.getSoldOutCatalog());
  }

  @PatchMapping
  public ApiResponse<SoldOutCatalogResponse> patchSoldOut(
      @RequestBody SoldOutPatchRequest request) {
    return ApiResponse.success(
        "ADMIN_SOLD_OUT_PATCH_SUCCESS", "품절 상태 저장 성공", adminSoldOutService.patchSoldOut(request));
  }
}
