package com.asak.admin.service;

import org.springframework.stereotype.Service;

// TODO-008: 품절 2/4 — Service/Mapper/DTO 구현.
// 1) 카탈로그 조회(getSoldOutCatalog)와 PATCH 대상(patchSoldOut)을 추가하고 targetType별 대상 조회를 분리한다.
// 2) targetType/targetId/isSoldOut 검증, 중복 target 병합/거절, 없는 target·0건 update 규칙을 ErrorCode와 맞춘다.
// 3) changes가 여러 건이면 전체 롤백/부분 성공 중 하나를 정하고 transaction으로 보장한 뒤 TODO-007/009/010과 연결한다.
@Service
public class AdminSoldOutService {}
