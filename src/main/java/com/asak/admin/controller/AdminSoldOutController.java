package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-040: 품절 1/4 — Controller 구현.
// 1) GET /api/admin/soldOut -> 카탈로그 응답
// 2) PATCH /api/admin/soldOut -> SoldOutPatchRequest { changes: [{ targetType, targetId, isSoldOut }] }
// 3) 성공/실패/ErrorCode 규격을 프론트 soldOutApi/useSoldOutDraft 와 맞춘다
// 4) 검증: 조회/저장 응답과 실패 코드가 draft 롤백 규칙과 맞는지 확인
@RestController
@RequestMapping("/api/admin/soldOut")
public class AdminSoldOutController {
}
