package com.asak.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asak.common.response.ApiResponse;
import com.asak.user.dto.payment.ApprovePaymentRequest;
import com.asak.user.dto.payment.ApprovePaymentResponse;
import com.asak.user.dto.payment.PaymentMethodListResponse;
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

    //     {
    //   "success": true,
    //   "status": 200,
    //   "code": "KIOSK_PAYMENT_METHOD_LIST_SUCCESS",
    //   "message": "결제수단 목록 조회 성공",
    //   "data": {
    //     "methods": [
    //       {
    //         "methodId": 10828,
    //         "methodCode": "CARD",
    //         "methodName": "카드·삼성페이",
    //         "isEnabled": true,
    //         "sortOrder": 1
    //       },
    //       {
    //         "methodId": 10829,
    //         "methodCode": "KAKAO_PAY",
    //         "methodName": "카카오페이",
    //         "isEnabled": false,
    //         "sortOrder": 2
    //       },
    //       {
    //         "methodId": 10830,
    //         "methodCode": "NAVER_PAY",
    //         "methodName": "네이버페이",
    //         "isEnabled": false,
    //         "sortOrder": 3
    //       }
    //     ]
    //   }
    // }

    // --------------- api-014 결제 수단 조회 ------------------------
    @GetMapping("payment-methods")
    public ApiResponse<PaymentMethodListResponse> payMethodList(){

        PaymentMethodListResponse response = payService.getPaymentMethod();

        return ApiResponse.success(
            "KIOSK_PAYMENT_METHOD_LIST_SUCCESS",
            "결제수단 목록 조회 성공",
            response);
    }


    //     {
    // "orderId": 1,
    // "paymentMethodCode": "CARD",
    // "idempotencyKey": "uuid"
    // }
    // --------------- api-006 결제 승인 ------------------------
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
