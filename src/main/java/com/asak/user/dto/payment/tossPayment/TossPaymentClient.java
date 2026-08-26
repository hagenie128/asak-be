package com.asak.user.dto.payment.tossPayment;

import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 토스페이먼츠 HTTP 호출 작업
// 내부 DB 멱등성
// → API-006 중복 처리 방지

// 토스 Idempotency-Key 헤더
// → 토스 승인 API 중복 호출 방지
@Component
public class TossPaymentClient {

  private final TossPaymentPropertie propertie;
  private final RestClient restClient;

  public TossPaymentClient(TossPaymentPropertie propertie) {
    this.propertie = propertie;
    this.restClient = RestClient.builder().baseUrl(propertie.getBaseUrl()).build();
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
      TossPaymentConfirmRequest request, String idempotencyKey) {

    return restClient
        .post()
        .uri("/v1/payments/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
            headers -> {
              headers.setBasicAuth(propertie.getSecretKey(), "");
              headers.set("Idempotency-Key", idempotencyKey);
            })
        .body(request)
        .exchange(
            (clientRequest, clientResponse) -> {
              if (clientResponse.getStatusCode().isError()) {
                TossErrorResponse error = clientResponse.bodyTo(TossErrorResponse.class);
                String errorCode = error == null ? null : error.code();

                throw mapTossError(clientResponse.getStatusCode().value(), errorCode);
              }

              return clientResponse.bodyTo(TossPaymentConfirmResponse.class);
            });
  }

  // 토스 HTTP 상태·오류 코드를 우리 서비스 ErrorCode로 변환
  private CustomException mapTossError(int tossHttpStatus, String tossErrorCode) {
    if (tossHttpStatus >= 500) {
      return new CustomException(ErrorCode.PAYMENT_NETWORK_ERROR);
    }

    String errorCode = tossErrorCode == null ? "" : tossErrorCode;

    return switch (errorCode) {
      case "REJECT_ACCOUNT_PAYMENT" -> new CustomException(ErrorCode.PAYMENT_INSUFFICIENT_FUNDS);
      case "REJECT_CARD_PAYMENT", "INVALID_REJECT_CARD" ->
          new CustomException(ErrorCode.PAYMENT_DECLINED);
      case "PROVIDER_ERROR",
          "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
          "FAILED_INTERNAL_SYSTEM_PROCESSING",
          "UNKNOWN_PAYMENT_ERROR" ->
          new CustomException(ErrorCode.PAYMENT_NETWORK_ERROR);
      case "INVALID_API_KEY", "UNAUTHORIZED_KEY", "INCORRECT_BASIC_AUTH_FORMAT" ->
          new CustomException(ErrorCode.TOSS_PAYMENT_CONFIGURATION_ERROR);
      case "NOT_FOUND_PAYMENT", "NOT_FOUND_PAYMENT_SESSION", "ALREADY_PROCESSED_PAYMENT" ->
          new CustomException(ErrorCode.TOSS_PAYMENT_APPROVAL_FAILED);
      default -> new CustomException(ErrorCode.TOSS_PAYMENT_APPROVAL_FAILED);
    };
  }
}
