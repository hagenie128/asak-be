# 보류 범위와 확장 API

> 상태: 보류. 아래 기능은 현재 `ASAK-back` 구현 계획과 Bruno 컬렉션의 API 범위 밖이다.

| 기능 | 관련 화면 | 현재 결정 |
| --- | --- | --- |
| 영수증 출력 | SCR-023 | 주문 완료 데이터 계약은 연계 가능하나 출력/프린터 API는 미정 |
| 멤버십·쿠폰 | SCR-024 | 회원, 적립, 쿠폰 검증·중복 정책이 미정 |
| QR/바코드 | Extension | 계약 및 보안 정책 미정 |
| 접근성 API | SCR-014 | 현재 UI 책임; 별도 백엔드 요구사항 없음 |
| WebSocket/TTS push | Admin Live Order | polling/이벤트 계약 미정 |

이 범위는 기존 주문·결제 API를 임의로 확장하지 않는다. Product Bible, DB 테이블, 화면 상태, 오류 코드가 확정된 뒤 별도 API ID·Bruno 요청·DTO부터 정의한다.
