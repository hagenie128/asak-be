package com.asak.common.exception;

import org.springframework.http.HttpStatus;

// Service 로직에서 터지는 에러를 공통 처리

public enum ErrorCode {
  INVALID_OPTION_SELECTION("1001", HttpStatus.BAD_REQUEST, "옵션 선택이 유효하지 않습니다."),

  MENU_NOT_FOUND("2001", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
  MENU_SOLD_OUT("2002", HttpStatus.CONFLICT, "메뉴가 품절되었습니다."),

  ORDER_PRICE_CHANGED("3001", HttpStatus.CONFLICT, "주문 가격이 변경되었습니다."),
  ORDER_NOT_FOUND("3002", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  INVALID_ORDER_STATUS_TRANSITION("3003", HttpStatus.CONFLICT, "주문 상태 전환이 유효하지 않습니다."),

  PAYMENT_METHOD_DISABLED("4001", HttpStatus.CONFLICT, "결제 방법이 비활성화되었습니다."),
  PAYMENT_ALREADY_APPROVED("4002", HttpStatus.CONFLICT, "결제가 이미 승인되었습니다."),
  PAYMENT_FAILED("4003", HttpStatus.BAD_REQUEST, "결제에 실패했습니다.");

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
