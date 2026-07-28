package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.user.service.UserPayService;

import lombok.RequiredArgsConstructor;

/*
* RestController 에서는 요청별 이노테이션을 적용시켜줘야함
*
* GetMapping -> 조회
* PostMapping -> 추가
* put -> 데이터 전체 수정
* patch -> 데이터 부분 수정
* delete -> 삭제
*
* */

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
