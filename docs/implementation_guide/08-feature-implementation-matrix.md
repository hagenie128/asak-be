# 전체 기능 구현 매트릭스

> 화면 카드에 다 담기지 않는 **기능 단위**까지 빠짐없이 확인하는 목록이다.  
> 구현을 시작할 때 행 하나를 골라 관련 화면 카드와 원본 문서를 함께 본다.

| 기능 | 연결 화면 | 지금 구현할 핵심 행동 | API/데이터 기준 | 상태·예외 |
| --- | --- | --- | --- | --- |
| 주문 유형 | SCR-001, 005 | 매장/포장 선택을 주문 생성까지 유지 | `orderType` | 선택 전 주문 금지, 새 주문 초기화 |
| 메뉴 탐색 | SCR-003 | 카테고리·태그·검색·품절 표시 | `GET /api/kiosk/menuList` | Loading/Empty/Error/비활성 |
| 메뉴 옵션 | SCR-004 | 필수/복수 옵션, 재료 제외, 알레르기, 수량 | `GET /api/kiosk/menuDetail/{menuId}` | 최소·최대, 메뉴/재료/옵션 품절 |
| 장바구니 편집 | SCR-004, 005 | draft 수정, 삭제, 전체 비우기, 합계 표시 | cart local state → order payload | 마지막 항목 Empty, 저장 취소/실패 |
| 서버 주문 검증 | SCR-005, 007 | 서버가 가격·품절·수량을 최종 결정 | `POST /api/kiosk/orders`, `totalAmount` | 중복 제출, 가격 변경, 품절 |
| 결제 수단 | SCR-007, 018 | 관리자 설정을 Kiosk 선택 가능 상태로 반영 | kiosk/admin payment methods | Disabled/Maintenance/전체 비활성 |
| 결제 멱등성·복구 | SCR-007, 008, 012 | 한 주문당 한 결제, 승인/실패 결과 복구 | `POST /api/kiosk/payments`, `idempotencyKey` | processing, 거절, network, 이미 승인 |
| 주문 완료·세션 종료 | SCR-008, 013 | 완료 정보 표시, 새 주문 초기화, timeout 복구 | `orderNo`, `approvedAmount`, `waitingOrderCount` | 뒤로 가기 재결제 차단 |
| 접근성 | SCR-001, 014 및 전 Kiosk | High Contrast에서도 같은 주문 흐름 | UI mode | 색만으로 상태 전달 금지, Reverted |
| 주문 운영 | SCR-009, 010 | 상세 확인, 상태 전이, 충돌 복구 | order detail/status API | Loading/Empty/Error/409 |
| 주방/TTS | SCR-009 | 상태 변경 성공 후 알림/TTS | order status 결과 | 새 주문 알림, TTS 실패, 중복 실행 |
| 품절 영향 | SCR-011, 003~005 | 메뉴/재료/옵션 품절이 Kiosk 전 구간에 반영 | `PATCH /api/admin/soldOut` | 저장 중/실패 복구, 전체 비활성 |
| 메뉴 CRUD | SCR-016 | 메뉴·재료·옵션 그룹 추가/수정/삭제 | admin menus/ingredients API | form 검증, 삭제 확인, 저장 복구 |
| 이미지·영양 계산 | SCR-016 | 메뉴 이미지 업로드와 영양값 계산 | `menuImages`, `nutrition/calculate` | 업로드/계산 중·실패, 이전 입력 유지 |
| 매출 집계 | SCR-019~021 | 기간/연도/일자별 KPI와 추이 표시 | sales summary/monthly/daily API | Loading/Empty/Error/Partial Data |
| 대시보드 판단 | SCR-022 | 매출·주문·대기·품절 현황 요약 | `GET /api/admin/dashboard` | 데이터 없음과 오류 구분 |
| 인증/권한 | SCR-015 및 Admin 보호 route | 로그인과 권한 없는 접근 차단 | 인증 계약 미확정 | validation, auth error, submitting, 401/403 |
| 영수증 출력 | SCR-023 | 선택·출력·재출력 | receipt API DTO 미확정 | printing, 프린터/용지/error |
| 멤버십·쿠폰 | SCR-024 | 식별·혜택 확인·적용 | extension 계약 미확정 | scanning, 중복 적립, 검증 실패 |

## 구현 순서가 막힐 때

1. 이 표의 기능 행을 고른다.
2. 연결 Screen ID 링크는 [전체 화면·기능 찾기](feature-lookup.md#화면-누락-확인표)에서 연다.
3. API가 `미확정`이면 화면용 가짜 endpoint를 만들지 말고 mock으로 표시한 뒤 계약 확정 항목으로 남긴다.
4. 기능의 모든 상태는 05-C/06-C와 [07-C QA](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=190-2)에서 비교한다.
