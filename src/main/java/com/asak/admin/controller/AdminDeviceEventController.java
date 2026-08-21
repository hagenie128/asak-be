package com.asak.admin.controller;

import com.asak.common.device.CreateDeviceEventRequest;
import com.asak.common.device.DeviceEventResponse;
import com.asak.common.device.DeviceEventService;
import com.asak.common.device.DevicePrintCommand;
import com.asak.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 재출력/로그 조회와, 예제 형식의 RTOS polling 결과 보고를 임시로 함께 둔다. RTOS 인증 정책이 정해지면 RTOS endpoint는 별도
 * Controller로 분리/ 임시 코드
 */
@RestController
@RequiredArgsConstructor
public class AdminDeviceEventController {
  private final DeviceEventService deviceEventService;

  @PostMapping("/api/admin/orders/{orderId}/receipt-print-text")
  public ApiResponse<DeviceEventResponse> requestReceiptPrint(
      @PathVariable long orderId, @Valid @RequestBody CreateDeviceEventRequest request) {
    DeviceEventResponse response =
        deviceEventService.createReceiptPrintEvent(orderId, request, "ADMIN");
    return ApiResponse.success("ADMIN_RECEIPT_PRINT_REQUESTED", "영수증 재출력 요청을 등록했습니다.", response);
  }

  @GetMapping("/api/admin/device-events")
  public ApiResponse<List<DeviceEventResponse>> findAll() {
    return ApiResponse.success(deviceEventService.findAll());
  }

  /** RTOS CommandPollTask가 PENDING 한 건을 가져간다. */
  @GetMapping("/api/rtos/device-events/pending")
  public ApiResponse<DeviceEventResponse> claimNextPendingEvent() {
    return deviceEventService
        .claimNextPendingEvent()
        .map(
            response ->
                ApiResponse.success("RTOS_DEVICE_EVENT_CLAIMED", "처리할 장치 이벤트입니다.", response))
        .orElseGet(() -> ApiResponse.success("RTOS_DEVICE_EVENT_EMPTY", "처리할 장치 이벤트가 없습니다.", null));
  }

  /** RTOS WorkerTask가 실제 Handler 실행 결과를 Spring Boot에 보고한다. */
  @PatchMapping("/api/rtos/device-events/{eventId}/finish")
  public ApiResponse<DeviceEventResponse> finishEvent(
      @PathVariable long eventId, @Valid @RequestBody FinishRequest request) {
    DeviceEventResponse response =
        deviceEventService.finishEvent(eventId, request.status(), request.result());
    return ApiResponse.success("RTOS_DEVICE_EVENT_FINISHED", "RTOS 처리 결과를 반영했습니다.", response);
  }

  public record FinishRequest(@NotNull DevicePrintCommand.Status status, @NotBlank String result) {}
}
