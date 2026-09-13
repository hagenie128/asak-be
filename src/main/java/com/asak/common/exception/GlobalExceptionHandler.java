package com.asak.common.exception;

import com.asak.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException exception) {
    ErrorCode errorCode = exception.getErrorCode();

    return ResponseEntity.status(errorCode.status()).body(ApiResponse.error(errorCode));
  }

  /**
   * 필수 요청 파라미터 누락. 예: /api/admin/sales/monthly 에 year 가 없는 경우.
   *
   * <p>이전에는 catch-all 로 떨어져 500 "서버 오류가 발생했습니다."가 나갔다. 서버는 멀쩡한데 고장난 것처럼 보여 원인 추적이 어려웠다. 클라이언트 잘못이므로
   * 400으로 돌려주고, 어떤 파라미터가 빠졌는지 알린다.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Object>> handleMissingParameter(
      MissingServletRequestParameterException exception) {
    log.warn("필수 요청 파라미터 누락: {}", exception.getParameterName());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.failure(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_REQUEST.code(),
                "필수 파라미터가 없습니다: " + exception.getParameterName()));
  }

  /** 요청 파라미터 타입 불일치. 예: year=2026 이어야 하는 자리에 year=abc 가 온 경우. 날짜·기간 필터에서 형식이 어긋날 때 이 경로로 들어온다. */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    log.warn("요청 파라미터 형식 오류: {}={}", exception.getName(), exception.getValue());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.failure(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.INVALID_REQUEST.code(),
                "파라미터 형식이 올바르지 않습니다: " + exception.getName()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception exception) {
    log.error("예상하지 못한 서버 오류", exception);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다."));
  }
}
