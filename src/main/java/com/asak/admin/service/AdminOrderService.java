package com.asak.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.LiveOrderListResponse;
import com.asak.admin.dto.response.LiveOrderResponse;
import com.asak.admin.mapper.AdminOrderMapper;

@Service
public class AdminOrderService {

  private static final int ORDER_LIST_LIMIT = 20;

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public Map<String, Object> getActiveOrders() {
    Map<String, Object> response = new HashMap<>();
    response.put("data", adminOrderMapper.getActiveOrders());
    return response;
  }

  public Map<String, Object> getOrderList() {
    OrderListFilter mapperFilter = OrderListFilter.builder()
        .offset(0)
        .limit(ORDER_LIST_LIMIT)
        .build();

    Map<String, Object> response = new HashMap<>();
    response.put("data", adminOrderMapper.getOrderList(mapperFilter));
    return response;
  }

  public Map<String, Object> getOrderDetail(Long orderId) {
    Map<String, Object> response = new HashMap<>();
    response.put("data", adminOrderMapper.getOrderDetail(orderId));
    return response;
  }
}
