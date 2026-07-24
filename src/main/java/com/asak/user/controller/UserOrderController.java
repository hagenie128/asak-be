package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.common.response.ApiResponse;
import com.asak.user.dto.CreateOrderRequest;
import com.asak.user.dto.CreateOrderResponse;
import com.asak.user.service.UserOrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


// ---[주문]---
// 장바구니 검증
// 주문 생성

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserOrderController {

    //주문 서비스 연결
    private final UserOrderService orderService;

    @PostMapping("/orders")
    public ApiResponse<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request){

        CreateOrderResponse response = orderService.createOrder(request);
        return ApiResponse.success(response);
    }

    




}
