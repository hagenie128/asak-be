package com.asak.common.device;

import jakarta.validation.constraints.NotBlank;

/** React가 영수증 출력 명령을 Spring Boot에 등록할 때 사용하는 요청이다. */
public record CreateDeviceEventRequest(
    @NotBlank String eventType, @NotBlank String payload, @NotBlank String requestId) {}
