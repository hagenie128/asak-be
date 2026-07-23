package com.asak.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// Service 로직에서 터지는 에러를 공통 처리

@Getter
public enum ErrorCode {
  MENU_NOT_FOUND(HttpStatus.NOT_FOUND),
  MENU_SOLD_OUT(HttpStatus.CONFLICT),
  INVALID_OPTION_SELECTION(HttpStatus.BAD_REQUEST),
  ORDER_PRICE_CHANGED(HttpStatus.CONFLICT),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVALID_ORDER_STATUS_TRANSITION(HttpStatus.CONFLICT),
  PAYMENT_METHOD_DISABLED(HttpStatus.CONFLICT),
  PAYMENT_ALREADY_APPROVED(HttpStatus.CONFLICT),
  PAYMENT_FAILED(HttpStatus.BAD_REQUEST);

  private final HttpStatus status;

  ErrorCode(HttpStatus status) {
    this.status = status;
  }
}