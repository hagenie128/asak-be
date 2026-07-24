package com.asak.admin.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.asak.admin.dto.response.OrderDetailResponse;

public interface AdminOrderMapper {

  Map<String, Object> getLiveOrders();

  // 주문 관리 상세 화면 전용. getLiveOrders()의 DTO와는 별개로 유지한다.
  OrderDetailResponse getOrderDetail(@Param("orderId") Long orderId);

}