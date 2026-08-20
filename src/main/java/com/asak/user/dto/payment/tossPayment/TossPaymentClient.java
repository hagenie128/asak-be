package com.asak.user.dto.payment.tossPayment;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.asak.user.dto.payment.query.TossPaymentAuth;

// 토스페이먼츠 HTTP 호출 작업
// 내부 DB 멱등성
// → API-006 중복 처리 방지

// 토스 Idempotency-Key 헤더
// → 토스 승인 API 중복 호출 방지
@Component
public class TossPaymentClient{


    private final TossPaymentPropertie propertie;
    private final RestClient restClient;

    public TossPaymentClient(TossPaymentPropertie propertie){
        this.propertie = propertie;
        this.restClient = RestClient.builder()
                        .baseUrl(propertie.getBaseUrl())
                        .build();
    }
    

    // 1. 토스에 보낼 Body 생성
    // paymentKey, orderId, amount

    // 2. Header 생성
    // Authorization: Basic {secretKey:를 Base64 인코딩한 값}
    // Idempotency-Key: idempotencyKey
    // Content-Type: application/json

    // 3. POST https://api.tosspayments.com/v1/payments/confirm 호출

    // 4. 성공 응답을 TossPaymentConfirmResponse로 변환하여 반환
    public TossPaymentConfirmResponse confirm(
        TossPaymentConfirmRequest request,
        String idempotencyKey
    ){

        return restClient
        .post()
        .uri("/v1/payments/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(headers -> {
          headers.setBasicAuth(propertie.getSecretKey(), "");
          headers.set("Idempotency-Key", idempotencyKey);
        })
        .body(request)
        .retrieve()
        .body(TossPaymentConfirmResponse.class);

    }


}
