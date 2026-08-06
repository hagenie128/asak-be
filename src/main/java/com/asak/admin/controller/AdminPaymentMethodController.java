package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-044: 결제수단 Controller 구현.
// 1) GET /api/admin/paymentMethods -> 목록 응답
// 2) PATCH /api/admin/paymentMethods/{paymentMethodId} -> PatchPaymentMethodRequest 바인딩
// 3) 저장 성공/실패/ErrorCode 규격을 paymentMethodsApi/usePaymentMethodDraft 와 맞춘다
@RestController
@RequestMapping("/api/admin/paymentMethods")
public class AdminPaymentMethodController {
}
