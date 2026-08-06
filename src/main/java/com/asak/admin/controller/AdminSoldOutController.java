package com.asak.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO-036: 품절 Controller 구현.
// 1) GET /api/admin/soldOut -> 카탈로그 응답
// 2) PATCH /api/admin/soldOut -> SoldOutPatchRequest 바인딩
// 3) 성공/실패/ErrorCode 규격을 프론트 soldOutApi/useSoldOutDraft 와 맞춘다
@RestController
@RequestMapping("/api/admin/soldOut")
public class AdminSoldOutController {
}
