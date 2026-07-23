package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.user.service.UserPayService;

import lombok.RequiredArgsConstructor;

// ---[결제]---
// 결제 승인
// 결제수단 조회

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserPayController {

    //결제 서비스 연결
    private final UserPayService payService;


}
