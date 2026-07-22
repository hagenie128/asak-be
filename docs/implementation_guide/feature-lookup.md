# 전체 화면·기능 찾기

> **빠짐 확인용 목록**이다. `화면 하나` 또는 `상태 하나`를 구현할 때 해당 작업 카드로 이동한다.  
> Screen Registry는 21개 화면을 정의하고, 05-C/06-C는 그 화면 안의 Loading·Empty·Error·Saving 등 세부 상태까지 보여 준다.

화면 상태·레이아웃은 승인된 [00-C 파일 맵](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=174-8727), [05-C 키오스크](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-7720), [06-C 관리자](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-10606), [07-C 상태 매트릭스](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=190-2)를 함께 본다.

| 작업 | 구현 가이드 | 화면 | 우선 확인할 원본 문서 |
| --- | --- | --- | --- |
| 전체 시작·충돌 판단 | [시작점](00-start-here.md) | 전체 | [Index](../governance/product-bible-index-2026-07-16.md), [Canonical Source](../product_bible/01_Foundation/docs/00-product-bible/CANONICAL_SOURCE.md), [Screen Registry](../product_bible/07_Screen_Bible/docs/07-screens/SCREEN_REGISTRY.md) |
| 홈·메뉴·옵션 | [Kiosk](02-kiosk-implementation.md) | SCR-001, 003, 004 | [Menu Detail Flow](../product_bible/03_Menu_Inventory_SoldOut/docs/09-features/menu/MENU_DETAIL_FLOW_AND_VALIDATION.md) |
| 장바구니 | [Kiosk](02-kiosk-implementation.md) | SCR-005 | [Cart State](../product_bible/02_Order_Cart_Payment/docs/09-features/cart/CART_STATE_AND_EVENT_FLOW.md) |
| 주문 생성·상태 | [Kiosk](02-kiosk-implementation.md), [API·DB](04-api-db-implementation.md) | SCR-005, 009, 010 | [Order Flow](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_FLOW_AND_STATE.md), [Order Architecture](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_ARCHITECTURE.md) |
| 결제·완료·실패 | [Kiosk](02-kiosk-implementation.md), [QA](06-qa-release-guide.md) | SCR-007, 008, 012 | [Payment Flow](../product_bible/02_Order_Cart_Payment/docs/09-features/payment/PAYMENT_FLOW_AND_STATE.md), [Order QA](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_EDGE_CASE_AND_QA.md) |
| timeout·접근성·오류 복구 | [Kiosk](02-kiosk-implementation.md), [QA](06-qa-release-guide.md) | SCR-013, 014 | [Timeout](../product_bible/05_Accessibility_Timeout_Error/docs/09-features/timeout-session/TIMEOUT_SESSION_ARCHITECTURE.md), [Accessibility](../product_bible/05_Accessibility_Timeout_Error/docs/09-features/accessibility/ACCESSIBILITY_ARCHITECTURE.md), [Error Recovery](../product_bible/05_Accessibility_Timeout_Error/docs/09-features/error-recovery/ERROR_RECOVERY_ARCHITECTURE.md) |
| 대시보드·주방·TTS | [Admin](03-admin-implementation.md) | SCR-022, 009 | [Dashboard](../product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/dashboard/DASHBOARD_ARCHITECTURE.md), [Kitchen](../product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/kitchen/KITCHEN_FLOW_AND_EDGE_CASE.md), [TTS](../product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/tts/TTS_ARCHITECTURE.md) |
| 품절·재고·메뉴 관리 | [Admin](03-admin-implementation.md) | SCR-011, 016 | [Sold-out](../product_bible/03_Menu_Inventory_SoldOut/docs/09-features/sold-out/SOLD_OUT_MANAGEMENT.md), [Inventory](../product_bible/03_Menu_Inventory_SoldOut/docs/09-features/inventory/INVENTORY_POLICY.md) |
| 결제 수단·매출 | [Admin](03-admin-implementation.md) | SCR-018, 019, 020, 021 | [Sales](../product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/sales/SALES_ARCHITECTURE.md), Payment feature docs |
| API·DTO·DB | [API·DB](04-api-db-implementation.md) | 전체 | [Order API Contract](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_API_CONTRACT.md), [API Design](../product_bible/06_Engineering_Bible/docs/05-api/API_DESIGN_RULES.md), [Database Rules](../product_bible/06_Engineering_Bible/docs/04-database/DATABASE_ENGINEERING_RULES.md) |
| Figma와 React 컴포넌트 | [UI·컴포넌트](05-ui-component-guide.md) | 전체 | [Component System](../product_bible/08_Component_Bible/docs/08-components/00-foundation/COMPONENT_SYSTEM.md), [Frontend Architecture](../product_bible/06_Engineering_Bible/docs/02-frontend/FRONTEND_ARCHITECTURE.md) |
| Figma 상태를 하나씩 구현 확인 | [상태 체크리스트](09-figma-state-checklist.md) | SCR-001~022 | [07-C QA / Screen State Matrix](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=190-2) |
| 테스트·시연·릴리스 | [QA](06-qa-release-guide.md) | 전체 | [QA Strategy](../product_bible/09_QA_Bible/docs/10-qa/00-strategy/QA_STRATEGY.md), [Regression Suite](../product_bible/09_QA_Bible/docs/10-qa/06-regression/REGRESSION_SUITE.md), [Release Checklist](../product_bible/09_QA_Bible/docs/10-qa/07-demo-release/RELEASE_CHECKLIST.md) |
| 영수증 출력 | [Extension](07-extension-implementation.md#scr-023--receipt-output) | SCR-023 | [Receipt Screen](../product_bible/07_Screen_Bible/docs/07-screens/SCR-023-RECEIPT-OUTPUT.md) |
| 멤버십·쿠폰 | [Extension](07-extension-implementation.md#scr-024--membership--coupon) | SCR-024 | [Membership Screen](../product_bible/07_Screen_Bible/docs/07-screens/SCR-024-MEMBERSHIP---COUPON.md) |

## 화면 누락 확인표

| 도메인 | 화면 카드 | 구현할 때 확인할 세부 기능 |
| --- | --- | --- |
| Kiosk | [SCR-001](02-kiosk-implementation.md#scr-001--home) | 주문 유형, 접근성 진입 |
| Kiosk | [SCR-003](02-kiosk-implementation.md#scr-003--menu-list) | 카테고리/태그/검색, 품절 카드, 목록 복구 |
| Kiosk | [SCR-004](02-kiosk-implementation.md#scr-004--menu-detail) | 옵션, 재료 제외, 알레르기, 수량, 장바구니 편집 |
| Kiosk | [SCR-005](02-kiosk-implementation.md#scr-005--cart) | 삭제, 전체 비우기, 주문 생성, 가격/품절 복구 |
| Kiosk | [SCR-007](02-kiosk-implementation.md#scr-007--payment) | 수단 조회, 비활성/점검, 중복 결제 차단 |
| Kiosk | [SCR-008](02-kiosk-implementation.md#scr-008--complete) | 주문 번호, 대기 수, 새 주문 초기화 |
| Kiosk | [SCR-012](02-kiosk-implementation.md#scr-012--payment-error) | 거절, 재시도, 네트워크 복구 |
| Kiosk | [SCR-013](02-kiosk-implementation.md#scr-013--timeout) | 경고, 계속 주문, 결제 중 보호 |
| Kiosk | [SCR-014](02-kiosk-implementation.md#scr-014--accessibility) | High Contrast, 일반 모드 복귀 |
| Admin | [SCR-015](03-admin-implementation.md#scr-015--login) | 검증, 인증 실패, 제출 중 |
| Admin | [SCR-022](03-admin-implementation.md#scr-022--dashboard) | Loading/Empty/Error/Partial Data |
| Admin | [SCR-009](03-admin-implementation.md#scr-009--live-order-board) | 새 주문, 상태 변경, TTS, 충돌 |
| Admin | [SCR-010](03-admin-implementation.md#scr-010--order-management) | 필터, 상세, 목록 복구 |
| Admin | [SCR-011](03-admin-implementation.md#scr-011--sold-out-management) | 메뉴/재료/옵션 품절, 저장 복구 |
| Admin | [SCR-016](03-admin-implementation.md#scr-016--menu-management) | 추가/수정/삭제, 이미지, 영양, 구성 |
| Admin | [SCR-018](03-admin-implementation.md#scr-018--payment-method-settings) | 수단 상태/순서, 전체 비활성 경고 |
| Admin | [SCR-019](03-admin-implementation.md#scr-019--sales-summary) | 기간, KPI, 추이, 데이터 없음 |
| Admin | [SCR-020](03-admin-implementation.md#scr-020--monthly-sales) | 연도/월 집계 |
| Admin | [SCR-021](03-admin-implementation.md#scr-021--daily-sales) | 날짜/시간대 집계 |
| Extension | [SCR-023](07-extension-implementation.md#scr-023--receipt-output) | 출력 선택/진행/실패/재출력 |
| Extension | [SCR-024](07-extension-implementation.md#scr-024--membership--coupon) | 스캔, 혜택, 검증/중복 방지 |
