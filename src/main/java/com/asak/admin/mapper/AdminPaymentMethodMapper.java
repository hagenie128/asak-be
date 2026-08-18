package com.asak.admin.mapper;

// 결제수단 구현 참고: 목록 SELECT.
// 1) payment method master + 설정 테이블 기준 조회 SQL 확정
// 2) 프론트가 쓰는 row shape(methodId, name, isActive, sortOrder, receiptMessage ...)로 맞춘다
// 3) XML SELECT 추가 후 Controller GET과 연결
// 결제수단 구현 참고: UPDATE.
// 1) methodId 기준 활성/정렬/영수증 문구 수정 SQL 추가
// 2) 변경 건수(0/1) 반환
// 3) Service/Controller가 저장 성공 여부 판단에 사용
public interface AdminPaymentMethodMapper {}
