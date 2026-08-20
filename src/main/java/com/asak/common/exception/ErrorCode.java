package com.asak.common.exception;

import org.springframework.http.HttpStatus;

/**
 * API business error catalog.
 *
 * <p>{@code code} is a stable API contract value; {@code status} is the HTTP response status.
 */
public enum ErrorCode {

  // Cart / menu / option validation
  INVALID_OPTION_SELECTION("INVALID_OPTION_SELECTION", HttpStatus.BAD_REQUEST, "옵션 선택이 유효하지 않습니다."),
  OPTION_ITEM_SOLD_OUT("OPTION_ITEM_SOLD_OUT", HttpStatus.CONFLICT, "선택한 옵션 아이템이 품절되었습니다."),
  ITEM_QUANTITY_LIMIT_EXCEEDED(
      "ITEM_QUANTITY_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "아이템 수량 제한 9개를 초과했습니다."),
  CART_QUANTITY_LIMIT_EXCEEDED(
      "CART_QUANTITY_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "장바구니 전체 수량 제한 30개를 초과했습니다."),
  CART_EMPTY("CART_EMPTY", HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),
  INVALID_INGREDIENT_EXCLUSION(
      "INVALID_INGREDIENT_EXCLUSION", HttpStatus.BAD_REQUEST, "제외할 수 없는 재료입니다."),
  MENU_NOT_FOUND("MENU_NOT_FOUND", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
  MENU_SOLD_OUT("MENU_SOLD_OUT", HttpStatus.CONFLICT, "메뉴가 품절되었습니다."),
  INVALID_ORDER_REQUEST("INVALID_ORDER_REQUEST", HttpStatus.BAD_REQUEST, "주문 요청이 올바르지 않습니다."),
  INVALID_ORDER_TYPE("INVALID_ORDER_TYPE", HttpStatus.BAD_REQUEST, "올바른 주문 유형이 아닙니다."),
  ORDER_INITIAL_STATUS_NOT_FOUND(
      "ORDER_INITIAL_STATUS_NOT_FOUND",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "주문 초기 상태 설정을 찾을 수 없습니다."),
  ORDER_NUMBER_CREATE_FAILED(
      "ORDER_NUMBER_CREATE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "주문번호를 생성할 수 없습니다."),
  ORDER_DAILY_SEQUENCE_EXCEEDED(
      "ORDER_DAILY_SEQUENCE_EXCEEDED", HttpStatus.INTERNAL_SERVER_ERROR, "일일 주문번호 생성 한도를 초과했습니다."),
  ORDER_CREATE_FAILED("ORDER_CREATE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "주문을 생성할 수 없습니다."),
  ORDER_ITEM_CREATE_FAILED(
      "ORDER_ITEM_CREATE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "주문 내역의 아이템을 생성할 수 없습니다."),
  ORDER_OPTION_CREATE_FAILED(
      "ORDER_OPTION_CREATE_FAILED",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "주문 내역의 아이템의 옵션을 생성할 수 없습니다."),
  ORDER_EXCLUSION_CREATE_FAILED(
      "ORDER_EXCLUSION_CREATE_FAILED",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "주문 내역의 아이템의 제외 재료를 생성할 수 없습니다."),

  // Order lifecycle
  ORDER_PRICE_CHANGED("ORDER_PRICE_CHANGED", HttpStatus.CONFLICT, "주문 가격이 변경되었습니다."),
  ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
  INVALID_ORDER_STATUS_TRANSITION(
      "INVALID_ORDER_STATUS_TRANSITION", HttpStatus.CONFLICT, "주문 상태 전환이 유효하지 않습니다."),
  ORDER_STATUS_CONFLICT(
      "ORDER_STATUS_CONFLICT", HttpStatus.CONFLICT, "주문 상태가 이미 변경되었습니다. 최신 상태를 다시 조회해 주세요."),
  ORDER_CANCEL_NOT_ALLOWED("ORDER_CANCEL_NOT_ALLOWED", HttpStatus.CONFLICT, "주문을 취소할 수 없는 상태입니다."),
  ORDER_REFUND_NOT_ALLOWED("ORDER_REFUND_NOT_ALLOWED", HttpStatus.CONFLICT, "주문을 환불할 수 없는 상태입니다."),
  INVALID_ORDER_QUERY("INVALID_ORDER_QUERY", HttpStatus.BAD_REQUEST, "잘못된 조회 조건입니다."),
  ORDER_AMOUNT_MISMATCH("ORDER_AMOUNT_MISMATCH", HttpStatus.CONFLICT, "주문 금액이 일치하지 않습니다."),

  // Payment lifecycle
  PAYMENT_CREATE_FAILED("PAYMENT_CREATE_FAILED", HttpStatus.CONFLICT, "결제 생성이 실패했습니다."),
  PAYMENT_METHOD_DISABLED("PAYMENT_METHOD_DISABLED", HttpStatus.CONFLICT, "결제 방법이 비활성화되었습니다."),
  PAYMENT_ALREADY_APPROVED("PAYMENT_ALREADY_APPROVED", HttpStatus.CONFLICT, "결제가 이미 승인되었습니다."),
  PAYMENT_DECLINED("PAYMENT_DECLINED", HttpStatus.CONFLICT, "결제가 거절되었습니다."),
  PAYMENT_INSUFFICIENT_FUNDS("PAYMENT_INSUFFICIENT_FUNDS", HttpStatus.CONFLICT, "잔액이 부족합니다."),
  PAYMENT_NETWORK_ERROR("PAYMENT_NETWORK_ERROR", HttpStatus.CONFLICT, "결제 서버에 연결할 수 없습니다."),
  PAYMENT_TIMEOUT("PAYMENT_TIMEOUT", HttpStatus.CONFLICT, "결제 응답 시간이 초과되었습니다."),
  IDEMPOTENCY_KEY_CONFLICT(
      "IDEMPOTENCY_KEY_CONFLICT", HttpStatus.CONFLICT, "이미 다른 결제 요청에 사용된 멱등성 키입니다."),
  PAYMENT_DUPLICATE("PAYMENT_DUPLICATE", HttpStatus.CONFLICT, "이미 처리 중인 결제가 있습니다."),
  CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
  MENU_IMAGE_SAVE_FAILED(
      "MENU_IMAGE_SAVE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 이미지 저장에 실패했습니다."),
  MENU_INSERT_FAILED("MENU_INSERT_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 등록에 실패했습니다."),
  MENU_CREATE_INVALID("MENU_CREATE_INVALID", HttpStatus.BAD_REQUEST, "메뉴 등록 요청이 올바르지 않습니다."),
  INGREDIENT_NOT_FOUND("INGREDIENT_NOT_FOUND", HttpStatus.NOT_FOUND, "재료 목록을 찾을 수 없습니다."),
  MENU_UPDATE_FAILED("MENU_UPDATE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 수정에 실패했습니다."),
  MENU_UPDATE_INVALID("MENU_UPDATE_INVALID", HttpStatus.BAD_REQUEST, "메뉴 수정 요청이 올바르지 않습니다."),
  MENU_UPDATE_NOT_FOUND("MENU_UPDATE_NOT_FOUND", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
  MENU_DELETE_FAILED("MENU_DELETE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 삭제에 실패했습니다."),
  MENU_DELETE_INVALID("MENU_DELETE_INVALID", HttpStatus.BAD_REQUEST, "메뉴 삭제 요청이 올바르지 않습니다."),
  MENU_DELETE_NOT_FOUND("MENU_DELETE_NOT_FOUND", HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
  MENU_OPTION_GROUP_NOT_FOUND(
      "MENU_OPTION_GROUP_NOT_FOUND", HttpStatus.NOT_FOUND, "옵션 그룹을 찾을 수 없습니다."),
  MENU_INGREDIENT_NOT_FOUND("MENU_INGREDIENT_NOT_FOUND", HttpStatus.NOT_FOUND, "재료를 찾을 수 없습니다."),

  // Sales
  SALES_SUMMARY_NOT_FOUND("SALES_SUMMARY_NOT_FOUND", HttpStatus.NOT_FOUND, "매출 요약을 찾을 수 없습니다."),
  DATE_RANGE_INVALID("DATE_RANGE_INVALID", HttpStatus.BAD_REQUEST, "날짜 범위가 유효하지 않습니다."),
  SALES_MONTHLY_NOT_FOUND("SALES_MONTHLY_NOT_FOUND", HttpStatus.NOT_FOUND, "월별 매출을 찾을 수 없습니다."),
  SALES_DAILY_NOT_FOUND("SALES_DAILY_NOT_FOUND", HttpStatus.NOT_FOUND, "일별 매출을 찾을 수 없습니다."),
  SALES_HOURLY_NOT_FOUND("SALES_HOURLY_NOT_FOUND", HttpStatus.NOT_FOUND, "시간별 매출을 찾을 수 없습니다."),
  SALES_INTERVAL_INVALID("SALES_INTERVAL_INVALID", HttpStatus.BAD_REQUEST, "시간 단위는 30분 또는 60분만 허용됩니다."),
  SALES_DASHBOARD_NOT_FOUND(
      "SALES_DASHBOARD_NOT_FOUND", HttpStatus.NOT_FOUND, "대시보드 매출을 찾을 수 없습니다."),
  START_DATE_REQUIRED("START_DATE_REQUIRED", HttpStatus.BAD_REQUEST, "날짜를 입력해주세요."),
  END_DATE_GREATER_THAN_TODAY(
      "END_DATE_GREATER_THAN_TODAY", HttpStatus.BAD_REQUEST, "종료 날짜는 오늘 이후일 수 없습니다."),
  START_DATE_GREATER_THAN_TODAY(
      "START_DATE_GREATER_THAN_TODAY", HttpStatus.BAD_REQUEST, "시작 날짜는 오늘 이후일 수 없습니다."),
  END_DATE_LESS_THAN_START_DATE(
      "END_DATE_LESS_THAN_START_DATE", HttpStatus.BAD_REQUEST, "종료 날짜는 시작 날짜보다 이전일 수 없습니다."),
  YEAR_GREATER_THAN_CURRENT_YEAR(
      "YEAR_GREATER_THAN_CURRENT_YEAR", HttpStatus.BAD_REQUEST, "현재 연도보다 이후일 수 없습니다."),
  YEAR_LESS_THAN_MIN_YEAR(
      "YEAR_LESS_THAN_MIN_YEAR", HttpStatus.BAD_REQUEST, "영업 시작 연도보다 이전일 수 없습니다."),
  MONTHLY_SALES_SUMMARY_NOT_FOUND(
      "MONTHLY_SALES_SUMMARY_NOT_FOUND", HttpStatus.NOT_FOUND, "월별 매출을 찾을 수 없습니다.");

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
