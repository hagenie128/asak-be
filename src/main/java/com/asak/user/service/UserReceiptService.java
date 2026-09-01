package com.asak.user.service;

import com.asak.common.device.CreateDeviceEventRequest;
import com.asak.common.device.DeviceEventResponse;
import com.asak.common.device.DeviceEventService;
import com.asak.common.exception.CustomException;
import com.asak.common.exception.ErrorCode;
import com.asak.user.mapper.UserPayMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReceiptService {

  private final UserPayMapper userPayMapper;
  private final DeviceEventService deviceEventService;

  public DeviceEventResponse createWaitingNumberPrintEvent(
      long orderId, String requestId, String requestSource) {

    Integer waitingOrderNo = userPayMapper.findDailyWaitingOrderNoByOrderId(orderId);

    if (waitingOrderNo == null) {
      throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
    }

    CreateDeviceEventRequest eventRequset =
        new CreateDeviceEventRequest(
            "PRINT_WAITING_NUMBER", String.valueOf(waitingOrderNo), requestId);

    return deviceEventService.createReceiptPrintEvent(orderId, eventRequset, requestSource);
  }
}
