package com.asak.admin.mapper;

// TODO-018: selectSalesSummary / selectSalesMonthly / selectSalesDaily / selectDashboard를 DTO 반환형으로
// 선언한다.
// 날짜 조건·완료 주문 상태·집계 시간대는 Controller 주석/판매 계약과 같은 기준을 쓰고, N+1 대신 집계 SQL 또는 검증된 view를 사용한다.
// XML resultMap/컬럼 alias를 DTO camelCase와 맞춘 뒤, 빈 기간·0 매출·경계 날짜를 Mapper 단위로 확인한다.
public interface AdminStatsMapper {}
