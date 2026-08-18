package com.asak.user.controller;

import com.asak.common.response.ApiResponse;
import com.asak.user.dto.order.request.CartValidateRequest;
import com.asak.user.dto.order.request.CreateOrderRequest;
import com.asak.user.dto.order.response.CartValidateResponse;
import com.asak.user.dto.order.response.CreateOrderResponse;
import com.asak.user.service.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

// ---[주문]---
// 장바구니 검증
// 주문 생성

@RestController
@RequestMapping("/api/kiosk")
@RequiredArgsConstructor
public class UserOrderController {

  // 주문 서비스 연결
  private final UserOrderService orderService;

  // 장바구니 검증(api-004)
  @PostMapping("/cart/validate")
  public ApiResponse<CartValidateResponse> cartValidate(@RequestBody CartValidateRequest request) {
    CartValidateResponse response = orderService.cartValidate(request);
    return ApiResponse.success(response);
  }

  // 주문 생성 (api-005)
  @PostMapping("/orders")
  public ApiResponse<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request) {

    CreateOrderResponse response = orderService.createOrder(request);
    return ApiResponse.success(response);
  }
}
