package com.asak.common.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** FreeRTOS 실행 전, RTOS Handler가 받은 명령을 콘솔로 재현하는 개발용 구현체다. */
@Component
public class ConsoleRtosGateway implements DeviceGateway {
  private static final Logger log = LoggerFactory.getLogger(ConsoleRtosGateway.class);

  @Override
  public void printReceipt(DevicePrintCommand command) {
    log.info(
        "[Console RTOS] PRINT_RECEIPT eventId={}, orderId={}, payload={}",
        command.eventId(),
        command.orderId(),
        command.payload());
  }
}
