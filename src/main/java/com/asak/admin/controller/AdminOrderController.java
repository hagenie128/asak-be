package com.asak.admin.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.service.AdminOrderService;
import com.asak.admin.dto.response.LiveOrderListResponse;
import com.asak.common.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/admin/orders")
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  public AdminOrderController(AdminOrderService adminOrderService) {
    this.adminOrderService = adminOrderService;
  }

  // 조회 조건:

  // orderStatus IN (RECEIVED, PREPARING)
  // 정렬:

  // createdAt ASC (오래 기다린 주문 우선)
  // Empty는 오류가 아니라 200과 빈 content/목록이다.
  // Live 보드용 DTO는 화면 전용 menus[], 경과시간 등으로 조립할 수 있다. 주문 관리 목록/상세의 items[],
  // optionItems[]와 억지로 같은 DTO로 만들지 않는다.

  @GetMapping
  public ApiResponse<Map<String, Object>> getOrders() {
    return ApiResponse.success(
        "ADMIN_ORDER_LIST_SUCCESS",
        "관리자 주문 목록 조회 성공",
        adminOrderService.getOrderList());
  }

  @GetMapping("/{orderId}")
  public ApiResponse<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
    return ApiResponse.success(
        "ADMIN_ORDER_DETAIL_SUCCESS",
        "관리자 주문 상세 조회 성공",
        adminOrderService.getOrderDetail(orderId));
  }

  @GetMapping("/active")
  public ApiResponse<LiveOrderListResponse> getActiveOrders() {
    return ApiResponse.success(
        "ADMIN_ACTIVE_ORDERS_SUCCESS",
        "관리자 활성 주문 조회 성공",
        adminOrderService.getLiveOrders());
  }

}
