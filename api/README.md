# ASAK Bruno API Contract Collection

> Status: PARTIALLY_IMPLEMENTED (2026-08-20)
> 경로·요청 본문 정본: 현재 Controller · DTO (`ASAK-back/src`)
> 비교용 계약: `IMPLEMENTATION_PLAN.md` · Product Bible API Contract

Bruno `api` 폴더는 실행 중인 백엔드와 맞춰 호출하기 위한 요청 모음이다.
구현된 endpoint는 Controller 메서드가 있는 것만 해당한다. `SPEC_ONLY`는 클래스 `@RequestMapping`만 있고 메서드가 없다 (호출 시 404).

## 사용 방법

1. Bruno에서 이 `api` 폴더를 Collection으로 연다.
2. `Local` 환경을 선택하고 `baseUrl`을 실행 중인 백엔드 주소로 맞춘다.
3. `Local` 환경의 ID(`menuId`, `orderId` 등)는 실제 데이터와 다르면 환경 변수만 바꾼다.

`methodId: 10828`은 키오스크 결제수단 `CARD`(화면 이름 `카드·삼성페이`)다.

## 구현 상태 (Controller 기준)

| 구분 | path | 상태 |
| --- | --- | --- |
| Health | `GET /api/health` | 구현. code `HEALTH_OK` |
| 키오스크 카테고리 | `GET /api/kiosk/categories` | 구현. code `OK` |
| 키오스크 메뉴 목록 | `GET /api/kiosk/menuList` | 구현. code `OK`. categoryId query 없음 |
| 키오스크 메뉴 상세 | `GET /api/kiosk/menuDetail/{menuId}` | 구현. code `OK` |
| 장바구니 검증 | `POST /api/kiosk/cart/validate` | 구현. code `OK` |
| 주문 생성 | `POST /api/kiosk/orders` | 구현. code `OK`. data.orderStatus = `READY` |
| 결제수단 목록 | `GET /api/kiosk/payment-methods` | 구현. code `KIOSK_PAYMENT_METHOD_LIST_SUCCESS` |
| 결제 승인 | `POST /api/kiosk/payments` | 구현. code `KIOSK_PAYMENT_APPROVED` |
| 관리자 주문 목록 | `GET /api/admin/orders` | 구현. code `ADMIN_ORDER_LIST_SUCCESS` |
| 관리자 주문 상세 | `GET /api/admin/orders/{orderId}` | 구현. code `ADMIN_ORDER_DETAIL_SUCCESS` |
| Live 주문 | `GET /api/admin/orders/live` | 구현. code `ADMIN_LIVE_ORDERS_SUCCESS` |
| 주문 상태 변경 | `PATCH /api/admin/orders/{orderId}/{status}` | 구현. code `ADMIN_ORDER_STATUS_CHANGE_SUCCESS` |
| 주문 취소 | `PATCH /api/admin/orders/{orderId}/cancel` | 구현. code `ADMIN_ORDER_CANCEL_SUCCESS` |
| 관리자 메뉴 | `/api/admin/menus` CRUD·카테고리·재료 | 구현 |
| 옵션 그룹 | `GET /api/admin/opts/groups`, `GET /api/admin/opts/{optionGroupId}` | 구현 |
| 품절 | `/api/admin/soldOut` | SPEC_ONLY (TODO-007) |
| 관리자 결제수단 | `/api/admin/paymentMethods` | SPEC_ONLY (TODO-011) |
| 대시보드 | `GET /api/admin/dashboard` | 구현 · `ADMIN_DASHBOARD_SUCCESS` |
| 매출 요약 | `GET /api/admin/sales/summary?period=today\|week\|month` | 구현 · `ADMIN_SALES_SUMMARY_SUCCESS` |
| 월별 매출 | `GET /api/admin/sales/monthly?year=YYYY` | 구현 · `ADMIN_SALES_MONTHLY_SUCCESS` |
| 일별 매출 | `GET /api/admin/sales/daily?from&to` | 구현 · `ADMIN_SALES_DAILY_SUCCESS` |
| 일별 시간대 매출 | `GET /api/admin/sales/daily/time-slots?date&intervalMinutes=30\|60` | 구현 · `ADMIN_SALES_TIME_SLOTS_SUCCESS` |
| 관리자 로그인 | `POST /api/admin/login` | SPEC_ONLY (TODO-027) |

## 요청 파일 형식

모든 HTTP 요청 `.bru`는 아래 순서를 따른다.

1. `meta` — `name`, `type: http`, `seq` (파일명 숫자와 동일)
2. method 블록 — `url`, `body`, `auth: none`
3. `headers` / `body:json` / `vars:pre-request` (필요 시)
4. `docs` — API 코드·경로·응답 code·주의사항
5. `tests` — **health만** 유지

파일명: `{seq}-{kebab-name}.bru`
JSON body는 camelCase만 사용한다.

## 파일 목록

### health

| seq | 파일 | 메서드 |
| --- | --- | --- |
| 1 | health-check | GET /api/health |

### kiosk

| seq | 파일 | 메서드 |
| --- | --- | --- |
| 00 | categories | GET /api/kiosk/categories |
| 01 | menu-list | GET /api/kiosk/menuList |
| 02 | menu-detail | GET /api/kiosk/menuDetail/{menuId} |
| 03 | cart-validate | POST /api/kiosk/cart/validate |
| 04 | create-order | POST /api/kiosk/orders |
| 05 | payment-methods | GET /api/kiosk/payment-methods |
| 06 | start-payment | POST /api/kiosk/payments |

### admin

| seq | 파일 | 메서드 |
| --- | --- | --- |
| 01 | live-orders | GET /api/admin/orders/live |
| 02 | order-list | GET /api/admin/orders |
| 03 | order-detail | GET /api/admin/orders/{orderId} |
| 04 | order-status | PATCH /api/admin/orders/{orderId}/{status} |
| 05 | menu-list | GET /api/admin/menus |
| 06 | menu-detail | GET /api/admin/menus/{menuId} |
| 07 | create-menu | POST /api/admin/menus |
| 08 | update-menu | PATCH /api/admin/menus/{menuId} |
| 09 | delete-menu | DELETE /api/admin/menus/{menuId} |
| 10 | menu-list-total | GET /api/admin/menus (필터 없음) |
| 11 | category-list | GET /api/admin/menus/categories |
| 12 | ingredient-list | GET /api/admin/menus/ingredients |
| 13 | sold-out-list | GET /api/admin/soldOut (SPEC_ONLY) |
| 14 | update-sold-out | PATCH /api/admin/soldOut (SPEC_ONLY) |
| 15 | payment-methods | GET /api/admin/paymentMethods (SPEC_ONLY) |
| 16 | update-payment-method | PATCH /api/admin/paymentMethods/{methodId} (SPEC_ONLY) |
| 17 | dashboard | GET /api/admin/dashboard |
| 18 | sales-summary | GET /api/admin/sales/summary?period |
| 19 | sales-monthly | GET /api/admin/sales/monthly?year |
| 20 | sales-daily | GET /api/admin/sales/daily?from&to |
| 21 | cancel-order | PATCH /api/admin/orders/{orderId}/cancel |
| 22 | option-group-list | GET /api/admin/opts/groups |
| 23 | option-group-detail | GET /api/admin/opts/{optionGroupId} |
| 24 | login | POST /api/admin/login (SPEC_ONLY) |
| 25 | sales-daily-time-slots | GET /api/admin/sales/daily/time-slots?date&intervalMinutes |

## 주의

- 성공 응답 assert는 **health**에만 둔다.
- 결제 승인은 DB `pay_method_cfg.active = true`인 결제수단만 허용한다. 구 계약명 `isEnabled`는 폐기.
- 주문/장바구니 요청의 옵션은 `items[].optionItems[]`이며, 각 항목은 `optionItemId`, `quantity`를 가진다. 재료 제외는 `excludedIngredientIds`다.
- 금액 응답은 `totalAmount`. 장바구니 항목 단가는 현재 DTO가 `unitPrice`다.
- 주문 생성·결제 승인·관리자 주문 API의 주문 상태 필드는 `orderStatus`다. envelope의 HTTP `status`와 혼동하지 않는다.
- 키오스크 카테고리·메뉴·장바구니·주문 생성은 `ApiResponse.success(data)`라서 code가 `OK`다. 결제·관리자 API는 API별 문자열 code를 쓴다.
- 메뉴 등록 `unit`은 `G`/`ML` 같은 UNIT_TYPE 코드다. 표시명(그램)은 쓰지 않는다.
- 메뉴 `imageUrl`은 `media_asset`에 있는 URL만 허용한다. 없으면 생략하고 `mediaAssetId`를 우선한다.
- 재료 목록 행 식별자는 `id`다 (`ingredientId` 아님).
- 메뉴 삭제는 soft delete (`deleted_at`). `ing` 마스터는 지우지 않는다.
- wiki `rest-api-spec.md` 정본 path는 `/api/kiosk/**`, `/api/admin/**`이다. 구 `/api/menus` 표는 폐기.
- 매출 View의 DB 적용은 Bruno 요청 파일 생성과 별개다. 현재 DB에 필요한 View가 없으면 해당 매출 요청은 서버에서 실패할 수 있다.

## 기준 문서

- `ASAK-back/IMPLEMENTATION_PLAN.md`
- `ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md`
- `ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md`
- `ASAK/docs/product_bible/**/*API*.md`
- `ASAK/docs/wiki/rest-api-spec.md`

모든 응답 계약은 `{ success, status, code, message, data }` 형식을 따른다.
`code`는 API별 문자열이다. (예: `HEALTH_OK`, `ADMIN_MENU_LIST_SUCCESS`, 키오스크 일부는 `OK`)
