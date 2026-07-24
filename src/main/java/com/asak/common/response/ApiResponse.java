package com.asak.common.response;

import com.asak.common.exception.ErrorCode;

import lombok.Builder;
import lombok.Getter;

// -- [응답 공통 Api] --
// Product Bible / IMPLEMENTATION_PLAN 공통 envelope:
// {
//   "success": true,
//   "status": 200,
//   "code": "MENU_LIST_SUCCESS",
//   "message": "메뉴 목록 조회 성공",
//   "data": { ... }
// }
//
// code는 API별 의미 있는 문자열을 쓴다. (레거시 "0000" 숫자 코드 사용 안 함)

@Getter
@Builder
public class ApiResponse<T> {

  private boolean success;
  private int status;
  private String code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return success("OK", "요청이 성공했습니다.", data);
  }

  public static <T> ApiResponse<T> success(String code, String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .status(200)
        .code(code)
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> failure(int status, String code, String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .status(status)
        .code(code)
        .message(message)
        .data(null)
        .build();
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode) {
    return ApiResponse.<T>builder()
        .success(false)
        .status(errorCode.status().value())
        .code(errorCode.code())
        .message(errorCode.message())
        .data(null)
        .build();
  }

  public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
    return ApiResponse.<T>builder()
        .success(false)
        .status(errorCode.status().value())
        .code(errorCode.code())
        .message(errorCode.message())
        .data(data)
        .build();
  }
}
