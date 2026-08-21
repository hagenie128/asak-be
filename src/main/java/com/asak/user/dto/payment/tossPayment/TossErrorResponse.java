package com.asak.user.dto.payment.tossPayment;

// 토스 오류 응답 전용 dto (토스로부터 받는 오류에 대한 응답)

public record TossErrorResponse(String code, String message) {}
