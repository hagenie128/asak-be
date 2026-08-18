package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-044: 결제수단 1/4 — Controller 구현.
// 1) TODO-045 Service/Mapper/DTO가 준비된 뒤 GET 목록과 PATCH /{paymentMethodId}를 추가한다.
//    PATCH body: active, sortOrder?, receiptMessage?; id는 path에서만 받고 body id와 혼용하지 않는다.
// 2) 현재 코드 경로는 `/api/admin/paymentMethods`(camelCase)다. Product Bible의 kebab-case 표기와 다르면
//    프런트 TODO-046을 시작하기 전에 Controller·API 상수·문서의 정본 경로를 하나로 확정한다.
// 3) 성공/실패/ErrorCode 규격을 paymentMethodsApi/usePaymentMethodDraft와 맞추고, 0건 갱신은 성공으로 숨기지 않는다.
// 4) 검증: 토글/정렬 저장 후 재조회, 존재하지 않는 id, 정렬 충돌(409), 유효성 실패를 API로 확인한다.
@RestController
@RequestMapping("/api/admin/paymentMethods")
public class AdminPaymentMethodController {
}
