package com.asak.admin.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.OrderDetailResponse;
import com.asak.admin.dto.response.OrderListResponse;
import com.asak.admin.dto.response.LiveOrderListResponse;
import com.asak.admin.dto.response.LiveOrderResponse;
import com.asak.admin.mapper.AdminOrderMapper;
import com.asak.common.response.PageResult;

@Service
public class AdminOrderService {

  private static final int MAX_ORDER_LIST_SIZE = 100;

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public LiveOrderListResponse getLiveOrders() {
    List<LiveOrderResponse> content = adminOrderMapper.getLiveOrders();
    return LiveOrderListResponse.builder()
        .content(content)
        .totalElements(content.size())
        .build();
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

  public int changeOrderStatus(OrderDetailResponse response, String status) {
    // TODO-003: response.orderStatus → status 허용 전이만 통과 (RECEIVED→PREPARING→COMPLETED), 아니면 0
    int statusId;
    // TODO-004: statusId 12/13 하드코딩 제거 — OrderStatus enum 또는 코드테이블 조회로 교체
    if ("PREPARING".equals(status))
      statusId = 12;
    else if ("COMPLETED".equals(status))
      statusId = 13;
    else
      return 0;
    Map<String, Object> map = new HashMap<>();
    map.put("orderId", response.getOrderId());
    map.put("statusId", statusId);
    // TODO-005 연동: map에 expectedStatusId 넣어 optimistic update (Mapper XML TODO-005)
    return adminOrderMapper.changeOrderStatus(map);
  }

  public int cancelOrder(Long orderId) {
    // TODO-008: 취소 가능 상태 검사 + paymentStatus APPROVED면 환불 정책 연동 후 Mapper 호출
    // TODO-009 연동: cancleOrder → cancelOrder 로 메서드명 교체 후 호출
    return adminOrderMapper.cancleOrder(orderId);
  }
}
