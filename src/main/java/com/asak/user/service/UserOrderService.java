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

    private final UserOrderMapper userOrderMapper;



    // 주문 생성
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        
        OrderType orderType = request.getOrderType();
        List<OrderItemRequest> items = request.getItems();

        return null;
    }

}


