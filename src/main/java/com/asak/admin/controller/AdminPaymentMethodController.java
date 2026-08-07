package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-044: 결제수단 1/4 — Controller 구현.
// 1) GET /api/admin/paymentMethods -> 목록 응답
// 2) PATCH /api/admin/paymentMethods/{paymentMethodId} -> PatchPaymentMethodRequest
//    body: isActive, sortOrder?, receiptMessage?
// 3) 저장 성공/실패/ErrorCode 규격을 paymentMethodsApi/usePaymentMethodDraft 와 맞춘다
// 4) 검증: 토글/정렬 저장 후 재조회 응답 shape와 실패 코드를 확인
@RestController
@RequestMapping("/api/admin/paymentMethods")
public class AdminPaymentMethodController {
}
