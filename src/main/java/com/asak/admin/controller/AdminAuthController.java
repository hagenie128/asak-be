package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-027: POST /api/admin/login Controller를 구현한다.
// 1) LoginRequest(username, password)를 @Valid로 받고 AuthenticationManager로 자격 증명을 검증한다.
// 2) TODO-028 JwtTokenProvider의 access token·만료 정보를 ApiResponse data로 반환한다. password·원문 예외는 반환하지
// 않는다.
// 3) TODO-031/032/033이 이 응답을 소비하므로 token key, expiresAt, 401 ErrorCode를 먼저 고정한다.
// 4) 검증: 정상 로그인, 잘못된 비밀번호, 없는 계정, 빈 body가 각각 성공/401/400 계약을 지키는지 확인한다.
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {}
