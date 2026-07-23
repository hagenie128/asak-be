package com.asak.common.exception;

import org.springframework.http.HttpStatus;

// Service 로직에서 터지는 에러를 공통 처리

public enum ErrorCode {
  MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다.", 404), // 404
  MENU_SOLD_OUT(HttpStatus.CONFLICT, "메뉴가 품절되었습니다.", 409), // 409
  INVALID_OPTION_SELECTION(HttpStatus.BAD_REQUEST, "옵션 선택이 유효하지 않습니다.", 400), // 400
  ORDER_PRICE_CHANGED(HttpStatus.CONFLICT, "주문 가격이 변경되었습니다.", 409),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.", 404),
  INVALID_ORDER_STATUS_TRANSITION(HttpStatus.CONFLICT, "주문 상태 전환이 유효하지 않습니다.", 409),
  PAYMENT_METHOD_DISABLED(HttpStatus.CONFLICT, "결제 방법이 비활성화되었습니다.", 409),
  PAYMENT_ALREADY_APPROVED(HttpStatus.CONFLICT, "결제가 이미 승인되었습니다.", 409),
  PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제에 실패했습니다.", 400);

  private final int code;
  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message, int code) {
    this.status = status;
    this.message = message;
    this.code = code;
  }

  public HttpStatus status() {
    return status;
  }

  public String message() {
    return message;
  }

  public int code() {
    return code;
  }
}