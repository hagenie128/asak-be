package com.asak.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.user.service.UserMenuService;

import lombok.RequiredArgsConstructor;

// ---[메뉴]---
// 카테고리 조회
// 메뉴 목록 조회
// 메뉴 상세 조회

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserMenuController {

    //메뉴 서비스 연결
    private final UserMenuService menuService;





}
