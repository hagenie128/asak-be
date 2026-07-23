package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.user.service.UserOrderService;

import lombok.RequiredArgsConstructor;

// ---[주문]---
// 장바구니 검증
// 주문 생성

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserOrderController {

    //주문 서비스 연결
    private final UserOrderService orderService;




}
