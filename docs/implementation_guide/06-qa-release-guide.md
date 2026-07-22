# QA·릴리스 구현 가이드

> 기능이 보인다고 완료가 아니다. Figma, API, DB, 상태값과 복구 흐름까지 확인해야 완료다.

## 테스트 순서

`Build → Smoke → Feature → Integration → Data Integrity → Regression → Accessibility → Demo → Release`

| 우선순위 | 대표 실패 |
| --- | --- |
| P0 | 주문 불가, 중복 결제, 금액 불일치, Cart 초기화, 상태 전이 오류, 품절 주문, 서버 가격 검증 누락 |
| P1 | Loading/Empty/Error 누락, 검색/필터 오류, TTS 중복, Dashboard 데이터 오류, 접근성 일부 미반영 |
| P2 | 카피, 간격, hover, 차트 미세 표현, 확장 기능 |

## 기능 완료 체크

- [ ] Build와 lint가 성공한다.
- [ ] Screen ID, route, 최신 Figma의 주요 상태가 일치한다.
- [ ] Default / Loading / Empty / Error / Disabled와 복구 행동을 확인했다.
- [ ] 프론트 요청·응답·오류 처리가 API 계약과 일치한다.
- [ ] 서버가 가격·품절·상태 전이를 최종 검증한다.
- [ ] Kiosk 주문 → 결제 → DB → Admin 상태/매출 흐름을 통합 점검했다.
- [ ] P0 전체를 통과했다.
- [ ] 접근성 모드와 timeout, 결제 처리 중 조작을 점검했다.
- [ ] 기존 기능 regression과 데모 동선을 통과했다.
- [ ] 승인된 05-C 또는 06-C의 해당 Frame 상태를 빠뜨리지 않았다.

## 07-C 상태 대조 방법

1. [07-C QA / Screen State Matrix](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=190-2)에서 해당 Screen ID의 모든 상태를 찾는다.
2. `Loading`, `Empty`, `Error`, `Saving`, `Success`, `Changed`, `Disabled`, `Sold-out`, `Retry`가 어떤 프론트 state/API 결과인지 표로 연결한다.
3. Prototype 연결률과 화면 존재 여부를 혼동하지 않는다. 연결되지 않은 Loading/Error/Annotation Frame은 의도적으로 Flow에 미연결일 수 있다.
4. `Mock settings`·`__manual-check` 항목은 API/DB 완료 전 실데이터 통과 조건으로 쓰지 않는다.
5. toast, confirm, warning countdown, 결제 차단, TTS 실패도 별도 QA 시나리오로 테스트한다.

## 주문·결제 P0 시나리오

1. 품절 메뉴 또는 옵션을 장바구니에 담은 뒤 주문 시도한다.
2. 가격 변경 뒤 주문을 시도해 변경 안내와 복구가 되는지 확인한다.
3. 주문 생성/결제 버튼을 연속 클릭해도 한 건만 처리되는지 확인한다.
4. 결제 실패 뒤 장바구니·주문 정보를 유지하고 재시도 가능한지 확인한다.
5. 관리자 완료 처리를 중복 요청해도 주문 상태·매출·TTS가 한 번만 반영되는지 확인한다.
6. 승인 후 뒤로가기로 재결제할 수 없는지 확인한다.

## 릴리스 전 확인

- 코드: build, lint, 치명적 console 오류 없음, secret 없음, env 예시, README.
- 데이터: seed/mock 일관성, 상태 코드, 금액.
- Figma: 최신 frame, 보이는 명세 잔여 없음, prototype/screenshot.
- 시연: 데모 계정, 안정적인 mock, 실패 대비 영상/스크린샷, 알려진 한계.

## 정본 링크

- [QA Strategy](../product_bible/09_QA_Bible/docs/10-qa/00-strategy/QA_STRATEGY.md)
- [Regression Suite](../product_bible/09_QA_Bible/docs/10-qa/06-regression/REGRESSION_SUITE.md)
- [Release Checklist](../product_bible/09_QA_Bible/docs/10-qa/07-demo-release/RELEASE_CHECKLIST.md)
- [Order Edge Case and QA](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_EDGE_CASE_AND_QA.md)
- [Error Recovery Architecture](../product_bible/05_Accessibility_Timeout_Error/docs/09-features/error-recovery/ERROR_RECOVERY_ARCHITECTURE.md)
