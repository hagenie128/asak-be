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

// TODO-007: 품절 1/4 — Controller 구현.
// 1) TODO-008 Service/Mapper/DTO가 준비된 뒤 GET 카탈로그와 PATCH를 추가한다.
// 2) PATCH body는 SoldOutPatchRequest { changes: [{ targetType, targetId, isSoldOut }] }로 고정하고,
//    targetType별 허용 대상과 빈 changes/중복 target 검증을 Controller 또는 Service 경계에 명시한다.
// 3) 성공/실패/ErrorCode 규격을 프런트 TODO-009/010과 맞추며, 부분 실패인지 전체 롤백인지 transaction 정책을 정한다.
// 4) 검증: 메뉴·재료 혼합 변경, 존재하지 않는 target, 0건 갱신, 동시 변경(409)을 API로 확인한다.
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
  public ApiResponse<SoldOutCatalogResponse> patchSoldOut(@RequestBody SoldOutPatchRequest request) {
    return ApiResponse.success(
        "ADMIN_SOLD_OUT_PATCH_SUCCESS", "품절 상태 저장 성공", adminSoldOutService.patchSoldOut(request));
  }
}
