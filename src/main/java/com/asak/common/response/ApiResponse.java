package com.asak.common.response;

import lombok.Builder;
import lombok.Getter;

// -- [응답 공통 Api] --
// {
//   "success": true,
//   "status": 200,
//   "code": "SUCCESS",
//   "message": "OK",
//   "data": {...
//   }
// }

// 모든 API의 반환 규격
// ** T는 제네릭(Generic)

@Getter
@Builder
public class ApiResponse<T> {

  private boolean success;
  private int status;
  private String code;
  private String message;
  private T data;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .status(200)
        .code("SUCCESS")
        .message("요청이 성공했습니다.")
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
}
