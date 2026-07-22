# Figma 상태 구현 체크리스트

> 화면 카드의 `완료 체크`를 실제 Figma 상태 프레임과 대조하는 표다.  
> 체크가 안 된 행은 구현 완료가 아니다. Figma Prototype 연결 여부와 화면 상태 존재 여부는 별개다.

## Kiosk · 05-C

**Figma:** [05-C Screens / Kiosk](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-7720)

| Screen | 확인할 상태 |
| --- | --- |
| SCR-001 Home | Default, 주문 유형 선택, High Contrast |
| SCR-003 Menu List | Default, Loading, Empty, Error, 품절 메뉴, 카테고리 비활성, 장바구니 추가 toast, 빈 장바구니 toast |
| SCR-004 Menu Detail | Default, 옵션 선택, Loading, Error, 알레르기 펼침, 메뉴 품절, 재료 품절, 베이스 품절, 옵션 품절, 수량 제한 toast, 장바구니 항목 수정, 저장 중, 저장 오류, 취소 확인 |
| SCR-005 Cart | Default, Empty, 삭제 확인, 마지막 항목 삭제, 전체 비우기, 옵션 수정 완료, 품절 수정 요구, 결제 차단 |
| SCR-007 Payment | 결제 수단 선택, 모든 수단 비활성, 결제 수단 불러오기 오류, Summary 접힘/펼침, Processing, 네트워크 오류, 재시도 중 |
| SCR-008 Complete | 결제 승인 완료, 주문 번호/대기 수 표시, 새 주문 진입 |
| SCR-012 Payment Error | 결제 거절, 재시도/장바구니 복귀 |
| SCR-013 Timeout | 만료, 경고 카운트다운, 계속 주문 |
| SCR-014 Accessibility | Default, High Contrast, Reverted |

## Admin · 06-C

**Figma:** [06-C Screens / Admin](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-10606)

| Screen | 확인할 상태 |
| --- | --- |
| SCR-015 Login | Default, 입력 검증, 인증 오류, 제출 중, Unauthorized |
| SCR-022 Dashboard | Default, Loading, Error, Empty, Partial Data |
| SCR-009 Live Order | Default, Loading, Empty, Error, 상세 열기, 상태 변경 확인, 저장 중, 성공, 저장 오류, TTS 실패, 새 주문 알림 |
| SCR-010 Order Management | Default, 상세 열기, 필터 적용, Loading, Empty, Error |
| SCR-011 Sold-out Management | Default, 항목 변경, 전체 비활성 확인, 저장 중, 성공, 저장 오류, Loading, Empty, Error |
| SCR-016 Menu Management | Default, 상세 추가, 상세 수정, 검증 오류, 삭제 확인, 저장 중, 성공, 저장 오류, Empty, Loading |
| SCR-018 Payment Method Settings | Default, 토글/순서 변경, 저장 확인, 저장 중, 성공, 오류, 전체 비활성, Loading, 불러오기 오류 |
| SCR-019 Sales Summary | Default, 필터, Partial Data, Loading, Empty, Error |
| SCR-020 Monthly Sales | Default, 월/연도 변경, Loading, Empty, Error |
| SCR-021 Daily Sales | Default, 날짜 변경, Loading, Empty, Error |

## 공통 판정

| 상태 | 구현 확인 |
| --- | --- |
| Loading | 요청 중 조작 가능 여부가 명확하고 이전 데이터를 현재 결과처럼 보이지 않는다. |
| Empty | 데이터 없음의 이유와 다음 행동이 있다. |
| Error | 실패 단계와 재시도/복귀가 있다. |
| Disabled | 비활성 이유를 색상 외에도 전달한다. |
| Saving/Processing | 같은 요청을 중복으로 보내지 않는다. |
| Success | 서버 성공 응답 뒤에만 UI를 확정한다. |

**QA 기준:** [07-C QA / Screen State Matrix](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=190-2)
