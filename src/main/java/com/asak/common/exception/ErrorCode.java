package com.asak.common.exception;

import org.springframework.http.HttpStatus;

// Service 로직에서 터지는 에러를 공통 처리.
// code 문자열은 Product Bible / IMPLEMENTATION_PLAN API 계약의 에러 코드명과 맞춘다.

public enum ErrorCode {
  INVALID_OPTION_SELECTION(
      "INVALID_OPTION_SELECTION", HttpStatus.BAD_REQUEST, "옵션 선택이 유효하지 않습니다."),

  MENU_NOT_FOUND("MENU_NOT_FOUND", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
  MENU_SOLD_OUT("MENU_SOLD_OUT", HttpStatus.CONFLICT, "메뉴가 품절되었습니다."),

  ORDER_PRICE_CHANGED("ORDER_PRICE_CHANGED", HttpStatus.CONFLICT, "주문 가격이 변경되었습니다."),
  ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  INVALID_ORDER_STATUS_TRANSITION(
      "INVALID_ORDER_STATUS_TRANSITION", HttpStatus.CONFLICT, "주문 상태 전환이 유효하지 않습니다."),
  ORDER_CANCEL_NOT_ALLOWED(
      "ORDER_CANCEL_NOT_ALLOWED", HttpStatus.CONFLICT, "주문을 취소할 수 없는 상태입니다."),

  PAYMENT_METHOD_DISABLED(
      "PAYMENT_METHOD_DISABLED", HttpStatus.CONFLICT, "결제 방법이 비활성화되었습니다."),
  PAYMENT_ALREADY_APPROVED(
      "PAYMENT_ALREADY_APPROVED", HttpStatus.CONFLICT, "결제가 이미 승인되었습니다."),
  PAYMENT_FAILED("PAYMENT_FAILED", HttpStatus.BAD_REQUEST, "결제에 실패했습니다.");

  private final String code;
  private final HttpStatus status;
  private final String message;

  ErrorCode(String code, HttpStatus status, String message) {
    this.code = code;
    this.status = status;
    this.message = message;
  }

  public HttpStatus status() {
    return status;
  }

  public String message() {
    return message;
  }

  public String code() {
    return code;
  }
}
