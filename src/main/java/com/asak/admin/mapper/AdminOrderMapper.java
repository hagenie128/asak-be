package com.asak.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.LiveOrderResponse;
import com.asak.admin.dto.response.OrderDetailResponse;
import com.asak.admin.dto.response.OrderListResponse;

public interface AdminOrderMapper {

  List<LiveOrderResponse> getLiveOrders();

  // 주문 관리 상세 화면 전용. getLiveOrders()의 DTO와는 별개로 유지한다.
  OrderDetailResponse getOrderDetail(@Param("orderId") Long orderId);

  List<OrderListResponse> getOrderList(@Param("filter") OrderListFilter filter);

  long countOrderList(@Param("filter") OrderListFilter filter);

  int changeOrderStatus(Map<String, Object> map);

  int cancleOrder(Long orderId);
}
