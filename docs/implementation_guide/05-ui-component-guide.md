# UI·컴포넌트 구현 가이드

> 화면을 새로 그리기 전에 실제 프로젝트의 기존 컴포넌트와 Figma Component를 먼저 확인한다. 이 문서는 디자인을 새로 정의하지 않는다.

## 화면을 구현하는 순서

1. Screen ID, route, Figma Frame/Node를 확인한다.
2. 화면을 Header / Section / Feature Component / Common Component / State View로 나눈다.
3. 기존 공통 컴포넌트와 비슷한 책임이 있는지 확인한다.
4. 재사용 가능하면 props를 확장하고, 특정 화면에만 묶인 규칙이면 feature/page에 둔다.
5. Default, Loading, Empty, Error, Disabled, 선택·처리·완료 상태를 모두 연결한다.

## 경계 기준

| 위치 | 넣을 내용 | 넣지 않을 내용 |
| --- | --- | --- |
| `pages` | route 단위 조합, API orchestration, 화면 상태 | API URL 문자열, 복잡한 계산, 범용 Button/Card |
| `components` | 재사용 UI와 명확한 props | 특정 page 전용 비즈니스 규칙 |
| `features` | cart validation, payment flow, timeout, sold-out, TTS 같은 기능 단위 | 화면 전체 layout |
| `store` | 여러 화면이 공유하는 client state | DOM 조작 |
| `api` | Axios client, endpoint, 응답 unwrap, 오류 표준화 | React component |

## 상태 UI 체크

- Loading: 이전 데이터 오해나 중복 클릭을 만들지 않는다.
- Empty: 비어 있는 이유와 다음 행동을 제공한다.
- Error: 실패한 작업, 재시도 가능 여부, 복귀 위치를 구분한다.
- Disabled: 사유를 보이거나 접근 가능한 안내를 둔다.
- Modal/overlay: 원본 화면 상태를 망가뜨리지 않고, 닫기·뒤로가기·focus 흐름을 확인한다.
- 금액·수량·품절·선택값은 목록/상세/요약에서 같은 기준으로 보인다.

## Figma와 코드가 다를 때

Screen Registry와 승인된 `05-C/06-C`가 화면 기준이다. 실제 코드와 충돌하면 기존 코드를 몰래 교체하지 말고, 차이와 영향 범위를 정리한 뒤 팀 결정에 맞춰 수정한다.

## C 화면 컴포넌트·상태 기준

- Kiosk: MenuCard, OptionGroup/OptionItem, CartItem, PaymentMethodCard, Empty/Error/Loading, ConfirmDialog, 알레르기·품절·수량 제한 안내.
- Admin: DataTableRow, OrderDetailRow, OrderStatusBadge, SoldOutItem/Toggle, PaymentMethodRow, SalesMetricCard, 저장 결과 Toast/ConfirmDialog.
- 상태 이름은 문구가 아니라 데이터 상태를 표현한다. 예: `saving`, `saveError`, `toggleProgress`, `paymentDeclined`, `allMethodsDisabled`.
- Figma가 `Mock settings` 또는 `__manual-check`로 남긴 결제수단·할인·환불·KPI는 API/DB 근거 없이 실데이터로 만들지 않는다.

## 정본 링크

- [Screen Registry](../product_bible/07_Screen_Bible/docs/07-screens/SCREEN_REGISTRY.md)
- [Component System](../product_bible/08_Component_Bible/docs/08-components/00-foundation/COMPONENT_SYSTEM.md)
- [Component Creation Rules](../product_bible/08_Component_Bible/docs/08-components/00-foundation/COMPONENT_CREATION_RULES.md)
- [Frontend Architecture](../product_bible/06_Engineering_Bible/docs/02-frontend/FRONTEND_ARCHITECTURE.md)
- [Product Principles](../product_bible/01_Foundation/docs/00-product-bible/PRODUCT_PRINCIPLES.md)
- [05-C Screens / Kiosk](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-7720)
- [06-C Screens / Admin](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-10606)
