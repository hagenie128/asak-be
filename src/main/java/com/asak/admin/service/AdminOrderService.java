package com.asak.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.LiveOrderListResponse;
import com.asak.admin.dto.response.OrderDetailResponse;
import com.asak.admin.dto.response.OrderListResponse;
import com.asak.admin.mapper.AdminOrderMapper;

@Service
public class AdminOrderService {

  private static final int ORDER_LIST_LIMIT = 20;

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public List<LiveOrderListResponse> getActiveOrders() {
    return adminOrderMapper.getActiveOrders();
  }

  public List<OrderListResponse> getOrderList() {
    OrderListFilter mapperFilter = OrderListFilter.builder()
        .offset(0)
        .limit(ORDER_LIST_LIMIT)
        .build();

    return adminOrderMapper.getOrderList(mapperFilter);
  }

  public OrderDetailResponse getOrderDetail(Long orderId) {
    return adminOrderMapper.getOrderDetail(orderId);
  }
}
