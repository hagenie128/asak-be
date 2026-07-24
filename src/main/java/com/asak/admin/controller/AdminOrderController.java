package com.asak.admin.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.admin.service.AdminOrderService;
import com.asak.common.response.ApiResponse;

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

  // {"success": true, "status": 200, "code": "ADMIN_ORDER_LIST_SUCCESS",
  // "message": "관리자 주문 목록 조회 성공", "data": {"content": [{"orderId": 1, "orderNo":
  // "ASAK-20260703-001", "orderType": "TAKE_OUT", "totalPrice": 8900,
  // "orderStatus": "RECEIVED", "paymentStatus": "PAID", "createdAt":
  // "2026-07-03T13:00:00", "items": [{"menuId": 364, "menuName": "스파이시 쉬림프 샌드위치",
  // "quantity": 1, "unitPrice": 8900, "optionItems": [{"optionItemId": 269,
  // "name": "크리미칠리", "quantity": 1}], "excludedIngredients": [{"ingredientId":
  // 169, "name": "양파"}]}]}], "totalElements": 1}}

}
