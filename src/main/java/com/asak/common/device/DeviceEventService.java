package com.asak.common.device;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * 예제의 CommandStore와 같은 역할이다. DB DDL/Mapper SQL이 준비되기 전까지는 메모리 큐로 PENDING → PROCESSING →
 * COMPLETED/FAILED 흐름만 검증한다.
 */
@Service
public class DeviceEventService {
  private final AtomicLong sequence = new AtomicLong();
  private final ConcurrentHashMap<Long, DevicePrintCommand> commands = new ConcurrentHashMap<>();

  public DeviceEventResponse createReceiptPrintEvent(
      long orderId, CreateDeviceEventRequest request, String requestSource) {
    long eventId = sequence.incrementAndGet();
    DevicePrintCommand command =
        new DevicePrintCommand(
            eventId,
            orderId,
            request.eventType(),
            request.payload(),
            request.requestId(),
            requestSource,
            DevicePrintCommand.Status.PENDING,
            null,
            Instant.now(),
            null);
    commands.put(eventId, command);
    return DeviceEventResponse.from(command);
  }

  /** RTOS polling이 처리할 가장 오래된 명령을 PROCESSING으로 바꾼 뒤 반환한다. */
  public synchronized Optional<DeviceEventResponse> claimNextPendingEvent() {
    return commands.values().stream()
        .filter(command -> command.status() == DevicePrintCommand.Status.PENDING)
        .min(Comparator.comparingLong((DevicePrintCommand command) -> command.eventId()))
        .map(
            command -> {
              DevicePrintCommand processing =
                  commands.computeIfPresent(command.eventId(), (id, current) -> current.start());
              return DeviceEventResponse.from(processing);
            });
  }

  public DeviceEventResponse finishEvent(
      long eventId, DevicePrintCommand.Status status, String result) {
    if (status == DevicePrintCommand.Status.PENDING
        || status == DevicePrintCommand.Status.PROCESSING) {
      throw new IllegalArgumentException("finish status must be COMPLETED or FAILED");
    }
    DevicePrintCommand command =
        commands.compute(
            eventId,
            (id, current) -> {
              if (current == null) {
                throw new IllegalArgumentException("device event not found: " + eventId);
              }
              return current.finish(status, result, Instant.now());
            });
    return DeviceEventResponse.from(command);
  }

  public DeviceEventResponse findEvent(long eventId) {
    DevicePrintCommand command = commands.get(eventId);
    if (command == null) {
      throw new IllegalArgumentException("device event not found: " + eventId);
    }
    return DeviceEventResponse.from(command);
  }

  public List<DeviceEventResponse> findAll() {
    return commands.values().stream()
        .sorted(
            Comparator.comparingLong((DevicePrintCommand command) -> command.eventId()).reversed())
        .map(DeviceEventResponse::from)
        .toList();
  }
}
