package com.asak.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.common.response.ApiResponse;
import com.asak.user.dto.payment.ApprovePaymentRequest;
import com.asak.user.dto.payment.ApprovePaymentResponse;
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

    //     {
    // "orderId": 1,
    // "paymentMethodCode": "CARD",
    // "idempotencyKey": "uuid"
    // }

    //결제 서비스 연결
    private final UserPayService payService;


    @PostMapping("/payments")
    public ApiResponse<ApprovePaymentResponse> approvePayment(
        @RequestBody ApprovePaymentRequest request
    ){


        ApprovePaymentResponse response = payService.createApprovePayment(request);

        return ApiResponse.success(
            "KIOSK_PAYMENT_APPROVED",
            "결제가 승인되었습니다.",
            response);
    }


}
