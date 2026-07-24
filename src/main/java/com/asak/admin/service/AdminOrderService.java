package com.asak.admin.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asak.admin.mapper.AdminOrderMapper;

@Service
public class AdminOrderService {

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public Map<String, Object> getLiveOrders() {
    return adminOrderMapper.getLiveOrders();
  }
}
