package com.asak.admin.service;

import com.asak.admin.dto.request.orders.OrderListFilter;
import com.asak.admin.dto.response.orders.LiveOrderListResponse;
import com.asak.admin.dto.response.orders.LiveOrderResponse;
import com.asak.admin.dto.response.orders.OrderDetailResponse;
import com.asak.admin.dto.response.orders.OrderListResponse;
import com.asak.admin.mapper.AdminOrderMapper;
import com.asak.common.response.PageResult;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminOrderService {

  private static final int MAX_ORDER_LIST_SIZE = 100;

  /** 상태변경 결과 — Controller가 ErrorCode로 매핑한다. */
  public enum StatusChangeResult {
    SUCCESS,
    /** 규칙상 허용되지 않는 전이 (예: RECEIVED→COMPLETED) */
    INVALID_TRANSITION,
    /** 조회 시점과 DB 상태가 달라 UPDATE 0건 */
    CONFLICT
  }

  private final AdminOrderMapper adminOrderMapper;

  public AdminOrderService(AdminOrderMapper adminOrderMapper) {
    this.adminOrderMapper = adminOrderMapper;
  }

  public LiveOrderListResponse getLiveOrders() {
    List<LiveOrderResponse> content = adminOrderMapper.getLiveOrders();
    return LiveOrderListResponse.builder().content(content).totalElements(content.size()).build();
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

    OrderListFilter mapperFilter =
        OrderListFilter.builder()
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

    OrderListFilter pagedFilter =
        OrderListFilter.builder()
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

  /**
   * MVP 허용 전이: RECEIVED→PREPARING, PREPARING→COMPLETED. 규칙 위반은 DB를 치기 전에 INVALID_TRANSITION. 규칙은
   * 맞는데 UPDATE 0건이면 CONFLICT(다른 요청이 먼저 변경).
   */
  public StatusChangeResult changeOrderStatus(OrderDetailResponse response, String status) {
    String current = response.getOrderStatus();
    if (!isAllowedTransition(current, status)) {
      return StatusChangeResult.INVALID_TRANSITION;
    }

    Long expectedStatusId = adminOrderMapper.findOrderStatusId(current);
    Long nextStatusId = adminOrderMapper.findOrderStatusId(status);
    if (expectedStatusId == null || nextStatusId == null) {
      return StatusChangeResult.INVALID_TRANSITION;
    }

    Map<String, Object> map = new HashMap<>();
    map.put("orderId", response.getOrderId());
    map.put("statusId", nextStatusId);
    map.put("expectedStatusId", expectedStatusId);

    int updated = adminOrderMapper.changeOrderStatus(map);
    if (updated == 0) {
      return StatusChangeResult.CONFLICT;
    }
    return StatusChangeResult.SUCCESS;
  }

  private boolean isAllowedTransition(String current, String next) {
    if (current == null || next == null) {
      return false;
    }
    if ("RECEIVED".equals(current) && "PREPARING".equals(next)) {
      return true;
    }
    if ("PREPARING".equals(current) && "COMPLETED".equals(next)) {
      return true;
    }
    return false;
  }

  public int cancelOrder(Long orderId) {
    // TODO-001: APPROVED 결제 취소 정책을 구체화한다.
    // 1) 미승인 주문 cancel과 승인 결제 refund를 분리하고, TODO-038 환불 endpoint가 승인 결제만 담당하도록 상태표를 확정한다.
    // 2) 결제 상태·주문 상태·외부 결제 취소가 필요하면 TODO-039의 별도 mapper/sql을 하나의 트랜잭션 경계로 연결한다.
    // 3) 현재 0 반환을 성공으로 감추지 않고, 행 수 0·허용하지 않는 상태·동시 변경을 Controller ErrorCode와 맞춘다.
    // APPROVED·COMPLETED·CANCELED 는 취소 불가
    OrderDetailResponse response = adminOrderMapper.getOrderDetail(orderId);
    if (response == null) {
      return 0;
    }
    if (response.getPaymentStatus().equals("APPROVED")) {
      return 0;
    }
    if (response.getOrderStatus().equals("COMPLETED")
        || response.getOrderStatus().equals("CANCELED")) {
      return 0;
    }
    Map<String, Object> map = new HashMap<>();
    map.put("canceledStatusId", adminOrderMapper.findOrderStatusId("CANCELED"));
    if (map.get("canceledStatusId") == null) {
      return 0;
    }
    map.put("orderId", response.getOrderId());
    return adminOrderMapper.cancelOrder(map);
  }
}
