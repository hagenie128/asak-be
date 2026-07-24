package com.asak.admin.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.mapper.AdminOrderMapper;

@Service
public class AdminOrderService {

  private static final int ORDER_LIST_LIMIT = 20;

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public Map<String, Object> getLiveOrders() {
    return adminOrderMapper.getLiveOrders();
  }

  public Map<String, Object> getOrderList() {
    OrderListFilter mapperFilter = OrderListFilter.builder()
        .offset(0)
        .limit(ORDER_LIST_LIMIT)
        .build();

    Map<String, Object> response = new HashMap<>();
    response.put("content", adminOrderMapper.getOrderList(mapperFilter));
    return response;
  }
}
