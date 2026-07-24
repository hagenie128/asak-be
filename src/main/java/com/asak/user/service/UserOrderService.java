package com.asak.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asak.common.enums.OrderType;
import com.asak.common.response.ApiResponse;
import com.asak.user.dto.CreateOrderRequest;
import com.asak.user.dto.CreateOrderResponse;
import com.asak.user.dto.OrderItemRequest;
import com.asak.user.mapper.UserOrderMapper;

@Service
public class UserOrderService {

    // ------ 주문 생성 API 플로우 ----
    // ① common_code에서 orderTypeId 조회
    // ② common_code에서 RECEIVED 상태 조회
    // ③ 메뉴 가격 조회
    // ④ 옵션 가격 조회
    // ⑤ totalPrice 계산
    // ⑥ orders INSERT
    // ⑦ 생성된 orderId 반환


    // 주문 생성
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        
        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


