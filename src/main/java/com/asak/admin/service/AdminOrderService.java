package com.asak.admin.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.OrderDetailResponse;
import com.asak.admin.dto.response.OrderListResponse;
import com.asak.admin.mapper.AdminOrderMapper;
import com.asak.common.response.PageResult;

@Service
public class AdminOrderService {

  private static final int MAX_ORDER_LIST_SIZE = 100;

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public List<OrderListResponse> getActiveOrders() {
    return adminOrderMapper.getActiveOrders();
  }

  public PageResult<OrderListResponse> getOrderList(
      int page,
      int size,
      String status,
      String paymentStatus,
      String orderType,
      LocalDate dateFrom,
      LocalDate dateTo,
      String keyword) {
    int safeSize = Math.min(Math.max(1, size), MAX_ORDER_LIST_SIZE);
    long totalElements;

    OrderListFilter mapperFilter = OrderListFilter.builder()
        .status(blankToNull(status))
        .paymentStatus(blankToNull(paymentStatus))
        .orderType(blankToNull(orderType))
        .startAt(dateFrom == null ? null : dateFrom.atStartOfDay())
        .endAt(dateTo == null ? null : dateTo.plusDays(1).atStartOfDay())
        .keyword(blankToNull(keyword))
        .limit(safeSize)
        .offset(0)
        .build();

    totalElements = adminOrderMapper.countOrderList(mapperFilter);
    int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / safeSize));
    int safePage = Math.min(Math.max(0, page), totalPages - 1);

    OrderListFilter pagedFilter = OrderListFilter.builder()
        .status(mapperFilter.getStatus())
        .paymentStatus(mapperFilter.getPaymentStatus())
        .orderType(mapperFilter.getOrderType())
        .startAt(mapperFilter.getStartAt())
        .endAt(mapperFilter.getEndAt())
        .keyword(mapperFilter.getKeyword())
        .limit(safeSize)
        .offset(safePage * safeSize)
        .build();

    List<OrderListResponse> content = adminOrderMapper.getOrderList(pagedFilter);
    return new PageResult<>(content, safePage, safeSize, totalElements);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  public OrderDetailResponse getOrderDetail(Long orderId) {
    return adminOrderMapper.getOrderDetail(orderId);
  }
}
