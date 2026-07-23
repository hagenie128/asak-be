package com.asak.admin.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.asak.admin.service.AdminOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/order")
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

  @GetMapping("path")
  public ModelAndView getLiveOrders() {
    ModelAndView view = new ModelAndView("admin/order/live");
    Map<String, Object> liveOrders = adminOrderService.getLiveOrders();
    view.addObject("liveOrders", liveOrders);

    return view;

  }

}
