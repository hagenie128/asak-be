package com.asak.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.request.OrderListFilter;
import com.asak.admin.dto.response.OrderDetailResponse;
import com.asak.admin.dto.response.OrderListResponse;
import com.asak.admin.dto.response.LiveOrderListResponse;
import com.asak.admin.dto.response.LiveOrderResponse;

public interface AdminOrderMapper {

  List<LiveOrderResponse> getLiveOrders();

  // 주문 관리 상세 화면 전용. getLiveOrders()의 DTO와는 별개로 유지한다.
  OrderDetailResponse getOrderDetail(@Param("orderId") Long orderId);

  List<OrderListResponse> getOrderList(@Param("filter") OrderListFilter filter);

  long countOrderList(@Param("filter") OrderListFilter filter);

  List<LiveOrderListResponse> getActiveOrders();

}
