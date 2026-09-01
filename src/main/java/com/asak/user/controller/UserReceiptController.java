package com.asak.user.controller;

import com.asak.common.device.CreateDeviceEventRequest;
import com.asak.common.device.CreatePrintRequest;
import com.asak.common.device.DeviceEventResponse;
import com.asak.common.device.DeviceEventService;
import com.asak.common.response.ApiResponse;
import com.asak.user.service.UserReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 키오스크의 영수증 출력 요청과 결과 확인 API/ 임시 코드 */
@RestController
@RequestMapping("/api/kiosk/orders")
@RequiredArgsConstructor
public class UserReceiptController {

  private final DeviceEventService deviceEventService;
  private final UserReceiptService receiptService;

  @PostMapping("/{orderId}/receipt-print")
  public ApiResponse<DeviceEventResponse> requestReceiptPrint(
      @PathVariable long orderId, @Valid @RequestBody CreateDeviceEventRequest request) {
    DeviceEventResponse response =
        deviceEventService.createReceiptPrintEvent(orderId, request, "KIOSK");
    return ApiResponse.success("KIOSK_RECEIPT_PRINT_REQUESTED", "영수증 출력 요청을 등록했습니다.", response);
  }

  @PostMapping("/{orderId}/waiting-number-print")
  public ApiResponse<DeviceEventResponse> requestOrderNoPrint(
      @PathVariable long orderId, @Valid @RequestBody CreatePrintRequest request) {

    DeviceEventResponse response =
        receiptService.createWaitingNumberPrintEvent(orderId, request.requestId(), "KIOSK");

    return ApiResponse.success(
        "KIOSK_WAITING_NUMBER_PRINT_REQUESTED", "주문 번호 출력 요청을 등록했습니다.", response);
  }

  @GetMapping("/device-events/{eventId}")
  public ApiResponse<DeviceEventResponse> findEvent(@PathVariable long eventId) {
    return ApiResponse.success(deviceEventService.findEvent(eventId));
  }
}
