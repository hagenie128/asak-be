package com.asak.common.device;

public interface DeviceGateway {
  /** 실제 RTOS 전송 구현 또는 개발용 콘솔 시뮬레이터가 구현한다. */
  void printReceipt(DevicePrintCommand command);
}
