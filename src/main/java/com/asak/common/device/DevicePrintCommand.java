package com.asak.common.device;

import java.time.Instant;

/** React 요청부터 RTOS 처리 결과까지 전달되는 영수증 출력 명령 모델/ 임시 코드. */
public record DevicePrintCommand(
    long eventId,
    long orderId,
    String eventType,
    String payload,
    String requestId,
    String requestSource,
    Status status,
    String result,
    Instant requestedAt,
    Instant completedAt) {
  public DevicePrintCommand start() {
    return new DevicePrintCommand(
        eventId,
        orderId,
        eventType,
        payload,
        requestId,
        requestSource,
        Status.PROCESSING,
        null,
        requestedAt,
        null);
  }

  public DevicePrintCommand finish(Status nextStatus, String nextResult, Instant now) {
    return new DevicePrintCommand(
        eventId,
        orderId,
        eventType,
        payload,
        requestId,
        requestSource,
        nextStatus,
        nextResult,
        requestedAt,
        now);
  }

  public enum Status {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
  }
}
