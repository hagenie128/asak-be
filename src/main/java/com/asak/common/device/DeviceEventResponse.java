package com.asak.common.device;

import java.time.Instant;

/** React와 RTOS가 공통으로 보는 장치 명령 상태 응답/ 임시 코드 */
public record DeviceEventResponse(
    long eventId,
    long orderId,
    String eventType,
    String payload,
    String requestSource,
    DevicePrintCommand.Status status,
    String result,
    Instant requestedAt,
    Instant completedAt) {
  public static DeviceEventResponse from(DevicePrintCommand command) {
    return new DeviceEventResponse(
        command.eventId(),
        command.orderId(),
        command.eventType(),
        command.payload(),
        command.requestSource(),
        command.status(),
        command.result(),
        command.requestedAt(),
        command.completedAt());
  }
}
