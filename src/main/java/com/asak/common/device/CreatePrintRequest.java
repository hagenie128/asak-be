package com.asak.common.device;

import jakarta.validation.constraints.NotBlank;

// 주문 번호를 받기 위한 request(주문번호만 출력을 위한 프론트엔드로 부터 받는 값)

public record CreatePrintRequest(@NotBlank String requestId) {}
