# 백엔드 정본 참조 팩

> 상태: Current · 확인일: 2026-08-19
> 목적: API를 구현할 때 Product Bible, Bruno, Admin/Kiosk mock, 현재 소스를 다시
> 흩어져 찾지 않도록 **실행에 필요한 내용만** 한 곳에 모은다. 이 문서는 계약 요약이며,
> 소스 코드를 대신하거나 구현 완료를 뜻하지 않는다.

## 1. 먼저 믿을 순서

1. 실제 `ASAK-back` 코드와 연결 DB 스키마
2. [`IMPLEMENTATION_PLAN.md`](../../IMPLEMENTATION_PLAN.md)의 2026-07-23 합의
3. 아래에 연결한 Product Bible·governance 계약
4. Bruno 요청 (`../../api/`) — 경로와 요청 body 검토용
5. Admin/Kiosk mock — 화면 표시·adapter 설계 참고용

서로 다르면 임의로 섞지 않는다. 차이를 이 문서의 **확정 전 충돌** 표에 기록하고,
팀이 결정하기 전에는 새 API를 만들지 않는다.

## 2. 현재 소스 실측

| 항목 | 현재 확인값 | 구현 시 의미 |
| --- | --- | --- |
| 빌드 | Spring Boot `4.0.7`, Java `25`, Gradle | Product Bible의 4.1.0 표기보다 실제 `build.gradle`을 우선한다. 버전을 임의 변경하지 않는다. |
| HTTP API | health, Kiosk 장바구니 검증·주문 생성, Admin 주문 조회·메뉴 조회·상태 변경 Controller mapping 존재 | 주문 생성은 저장/응답 조립이 미완료다. 결제·취소 등 mapping이 없는 Bruno 요청은 `SPEC_ONLY`로 유지한다. |
| 공통 응답 | `ApiResponse<T>`에 `success`, `status`, `code`, `message`, `data` 필드 존재 | factory·예외 handler와 실제 Controller 적용은 남아 있다. |
| 페이지 응답 | `PageResult`는 빈 골격 | 목록 API 전 page 0/1-base, size, totalElements/totalPages 모양을 먼저 확정한다. |
| 주문 DTO | `CreateOrderRequest`, `OrderItemRequest`, `OptionItemRequest`, `CreateOrderResponse` 존재 | 요청 옵션은 `optionItems[]`를 사용한다. 주문 생성 DTO의 상태 필드는 현재 `status`이며, 저장·응답 조립은 아직 필요하다. |
| 상태 enum | `OrderStatus`: RECEIVED/PREPARING/COMPLETED/CANCELED, `PaymentStatus`: READY/APPROVED/FAILED/CANCELED/REFUNDED | 신규 API는 이 코드값을 사용한다. |

**현재 파일 위치**

- 공통 응답: `src/main/java/com/asak/common/response/ApiResponse.java`
- 상태 enum: `src/main/java/com/asak/common/enums/OrderStatus.java`, `PaymentStatus.java`
- 관리자 시작점: `src/main/java/com/asak/admin/controller/AdminOrderController.java`
- 관리자 Mapper XML: `src/main/resources/mappers/AdminOrderMapper.xml`
- Bruno Admin 요청: `api/admin/01-live-orders.bru` ~ `16-cancel-order.bru`

## 3. 모든 API의 공통 계약

```json
{
  "success": true,
  "status": 200,
  "code": "0000",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

- Java/JSON은 camelCase, DB는 현재 snake_case를 사용한다.
- API `code`는 숫자 문자열이다. 성공은 `"0000"`, 옵션 오류는 `"1001"`, 메뉴 오류는 `"2001"~`, 주문 오류는 `"3001"~`, 결제 오류는 `"4001"~`로 구분한다. HTTP `status` 숫자와 업무 코드는 다른 값이다.
- Entity 또는 Mapper 조회 결과를 그대로 반환하지 않는다. Controller는 validation/HTTP,
  Service는 비즈니스 규칙·트랜잭션, Mapper/XML은 SQL·조인·집계를 맡는다.
- `400`은 입력 검증, `404`는 대상 없음, `409`는 품절·가격·상태 충돌, `500`은 예상 밖 오류다.
- 금액·품절·주문/결제 상태는 클라이언트가 정하지 않는다. Service가 DB 기준으로 결정한다.

| DB 컬럼 | API 필드 | 규칙 |
| --- | --- | --- |
| `orders.total_price` | `totalAmount` | 서버가 재계산한 정본 금액 |
| `payment.amount` | `approvedAmount` | 승인/환불 판단에 사용 |
| `payment.paid_at` | `approvedAt` | 환불 후에도 원 승인 시각 보존 |
| `orders.canceled_at` | `canceledAt` | 주문 취소 시각 |
| `payment.refunded_at` | `refundedAt` | 승인 결제 환불 시각 |
| `menu.cat_id` | `categoryId` | `categoryCode`를 새 계약에 쓰지 않음 |
| `menu.sold_out` | `isSoldOut` | 메뉴 판매 가능 여부 |

## 4. Kiosk: 주문 생성과 결제에서 바로 쓸 계약

### 주문 생성 — API-005

```http
POST /api/kiosk/orders
```

구현에 필요한 요청 데이터는 `orderType`, `items[].menuId`, `quantity`, `optionItems[]`
(`optionItemId`, `quantity`), `excludedIngredientIds`다. 서버는 메뉴·옵션·필수 옵션·품절·수량을 다시 검사하고
`totalAmount`를 계산한다. 성공 응답 data는 `orderId`, `orderNo`, `totalAmount`,
`orderStatus: READY`다. API-005는 결제 전 주문을 `READY`로 저장한다.

저장 순서는 **주문 헤더 → 주문 아이템 → 선택 옵션/재료 제외**이며 하나의 transaction으로
처리한다. 장바구니 합계가 달라졌거나 품절이면 `409`로 중단하고, 프론트가 장바구니를
수정할 수 있도록 code를 반환한다.

### 결제 승인 — API-006

```http
POST /api/kiosk/payments
```

```json
{
  "orderId": 1,
  "orderStatus": "RECEIVED",
  "paymentMethodCode": "TOSS_PAY",
  "idempotencyKey": "uuid",
  "tossPayment": {
    "paymentKey": "tgen_20260819...",
    "orderId": "A202607230001",
    "amount": 8900
  }
}
```

`tossPayment`는 토스페이먼츠 승인에 필요한 정보다. 최상위 `orderId`는 백엔드의 주문 PK이고,
`tossPayment.orderId`는 토스페이먼츠에 전달한 주문번호다. `tossPayment.amount`는
`orders.total_price`와 일치해야 한다.

성공 data는 `paymentId`, `orderId`, `orderNo`, `paymentMethodCode`,
`paymentStatus: APPROVED`, `orderStatus: RECEIVED`, `approvedAmount`,
`waitingOrderCount`, `approvedAt`이다. 백엔드는 DB 주문 상태가 `READY`인지 확인하고,
승인 성공 뒤 `READY → RECEIVED`로 변경한다. `CARD`는 내부 mock 승인으로 처리하고,
`TOSS_PAY`, `KAKAO_PAY`, `NAVER_PAY`는 프론트가 받은 `tossPayment` 인증 결과로 토스페이먼츠
승인 API를 호출한다. 결제수단은 DB에서 활성 상태여야 한다.

## 5. Admin: 첫 세로 기능은 실시간 주문 — API-021 + API-008

### 활성 주문 조회 — API-021

```http
GET /api/admin/orders/live
```

- 조회 조건: `orderStatus IN (RECEIVED, PREPARING)`
- 정렬: `createdAt ASC` (오래 기다린 주문 우선)
- Empty는 오류가 아니라 `200`과 빈 `content`/목록이다.
- Live 보드용 DTO는 화면 전용 `menus[]`, 경과시간 등으로 조립할 수 있다. 주문 관리
  목록/상세의 `items[]`, `optionItems[]`와 억지로 같은 DTO로 만들지 않는다.

### 상태 변경 — API-008

```http
PATCH /api/admin/orders/{orderId}/{status}
```

Path: `orderId`, `status` (`PREPARING` 또는 `COMPLETED`). Request body는 없다.

허용 전이는 아래 두 개다.

```text
RECEIVED  → PREPARING
PREPARING → COMPLETED
```

응답 data는 `orderId`, `orderNo`, `previousStatus`, `status`, `updatedAt`을 반환한다.
이미 `COMPLETED`인 재요청은 현재 상태를 idempotent하게 반환하되, 완료 event·TTS·매출을
중복 생성하지 않는다. Backend는 TTS를 실행하지 않으며 Frontend가 성공 응답 뒤 실행한다.
동시에 바뀐 상태나 허용되지 않은 전이는 `409 INVALID_ORDER_STATUS_TRANSITION`으로
응답하고 최신 주문을 다시 읽게 한다.

### 목록·상세·취소 — API-007/022/024

- 목록 filter: `orderStatus`, `paymentStatus`, `orderType`, `dateFrom`, `dateTo`, `keyword`, `page`, `size`
- 상세: item, option, payment, timestamp를 조립하고 없으면 `404 ORDER_NOT_FOUND`
- 취소는 `RECEIVED` 또는 `PREPARING`일 때만 가능하다.
- 취소 시 order=`CANCELED` + `canceledAt`, 승인 결제라면 payment=`REFUNDED` +
  `refundedAt`으로 변경한다. 원 `approvedAt`은 지우지 않는다.
- 완료/이미 취소 주문 취소는 `409 ORDER_CANCEL_NOT_ALLOWED`다.
- 제품 전달 후 환불은 주문 취소가 아닌 별도 환불 API로 처리한다. order=`COMPLETED`는 유지하고
  payment=`REFUNDED` + `refundedAt`만 변경한다.
- 완료되지 않았거나 결제가 승인되지 않았거나 이미 환불된 주문의 환불은
  `409 ORDER_REFUND_NOT_ALLOWED`다.

## 6. Admin: 품절·결제수단·매출에서 바로 쓸 규칙

### 품절 — API-009/010

`MENU`, `INGREDIENT`, `OPTION_ITEM`의 code를 API·React·Figma에서 동일하게 쓴다.
저장은 여러 변경을 한 번에 받는 `PATCH /api/admin/soldOut`이며, 요청 형식은 다음과 같다.

```json
{
  "changes": [
    { "targetType": "INGREDIENT", "targetId": 33, "isSoldOut": true }
  ]
}
```

성공 data는 최소 `updatedCount`, `affectedMenuCount`다. 화면의 draft/확인/Toast는
프론트 책임이며, Backend는 실제 변경과 영향 범위를 원자적으로 검증한다.

### 결제수단 — API-015/016

정본 code는 `CARD`, `KAKAO_PAY`, `NAVER_PAY`다. Admin mock의 `CASH`, `zero`는 지원 여부와
ID 타입을 별도 결정하기 전 API에 추가하지 않는다. 설정 API는 `status`, `sortOrder`,
`displayName`, `receiptMessage`, `failureRetentionMinutes`를 다룬다.

### 매출·대시보드 — API-017~020

- 조회 원본: `vw_sales_daily`, `vw_sales_hourly`, `vw_top_menu_daily`, `vw_top_menu_hourly`
- 날짜: `YYYY-MM-DD`, timezone: `Asia/Seoul`; amount는 integer, ratio는 `0~1`
- gross sales: `paid_at`이 있는 모든 원결제 금액
- canceled amount: `CANCELED`/`REFUNDED`에 해당하는 승인 결제 금액
- net sales: `grossSalesAmount - canceledAmount`; 전액 환불은 0이며 음수가 되면 안 됨
- 인기 메뉴 뷰는 `paid_at`이 있고 order/payment가 취소·환불되지 않은 item만 포함

## 7. 프론트 mock과 API를 연결하는 정규화 표

| 현재 mock/store | 백엔드 정본 | 연결 위치 |
| --- | --- | --- |
| `totalPrice` | `totalAmount` | API adapter/repository |
| `CANCELLED` | `CANCELED` | API adapter/repository |
| `PAID` | `APPROVED` | Admin 표시 adapter 또는 화면 코드 통일 |
| `waitingCount` | `waitingOrderCount` | Kiosk `orderAdapter` |
| `amount`, `paidAt` | `approvedAmount`, `approvedAt` | payment adapter |
| `/api/admin/sold-out-items` | `/api/admin/soldOut` | API client 상수 |

API는 항상 정본 값을 반환한다. adapter가 legacy mock을 변환하며, Page/컴포넌트가 두 표현을
직접 혼용하지 않는다.

## 8. 확정 전 충돌 — 구현 전에 팀 결정 필요

| 항목 | 현재 차이 | 이 문서의 처리 |
| --- | --- | --- |
| 주문 요청 옵션 | Product Bible `ORDER_API_CONTRACT`에는 `selectedOptionItemIds` 표기가 남아 있고, 현재 DTO·구현 계획·Bruno는 `optionItems[]`를 사용한다. | 실제 백엔드 요청 계약은 `optionItems[]`로 통일한다. Product Bible 원문 갱신은 별도 문서 작업으로 남긴다. |
| 주문 생성 상태 필드 | Product Bible은 `orderStatus`를 사용하지만, 현재 `CreateOrderResponse`는 `status`다. | 이번 Bruno는 실제 DTO의 `status`를 따른다. `orderStatus` 통일은 DTO·호출부 변경과 함께 별도 구현 작업으로 처리한다. |
| 메뉴 관리 | Product Bible Draft에 `categoryCode`, `isActive`, DELETE가 있음 | 실제 DB에는 category code와 `menu.active` 근거가 없고 삭제 정책도 보류다. API-012/013만 현재 합의 범위로 구현한다. |
| 프레임워크 버전 | Product Bible은 Spring Boot 4.1.0, `build.gradle`은 4.0.7 | 실제 `build.gradle` 유지. 별도 팀 승인 없이 올리지 않는다. |

## 9. 구현·검증 순서와 Bruno 요청

1. `ApiResponse` factory, ErrorCode, GlobalExceptionHandler, PageResult 정책을 한 번 정한다.
2. `GET /api/health`로 Spring/MyBatis/DB 응답 경로를 먼저 확인한다.
3. Kiosk 메뉴 조회 → cart validate → 주문 생성 → 결제수단 → 결제 승인 순서로 구현한다.
4. Admin은 active orders → status update → list/detail → cancel/refund 순서로 재사용한다.
5. 그 뒤 품절·메뉴·결제수단 → 매출/대시보드로 진행한다.

각 API는 **Controller → Service → Mapper interface → Mapper XML → response DTO → 테스트 →
Bruno**까지 같은 작업에서 끝낸다. 2xx만 보지 말고 400/404/409/500 envelope를 함께 확인한다.

| 기능 | Bruno 요청 |
| --- | --- |
| Admin Live 주문 | `../../api/admin/01-live-orders.bru` |
| Admin 주문 목록·상세·상태 | `02-order-list.bru`, `03-order-detail.bru`, `04-order-status.bru` |
| Admin 취소/환불 | `16-cancel-order.bru` |
| Admin 품절 | `09-get-sold-out.bru`, `09-sold-out.bru` |
| Admin 결제수단·통계 | `10-payment-methods.bru` ~ `15-sales-daily.bru` |
| Kiosk 메뉴·주문·결제 | `../../api/kiosk/00-categories.bru` ~ `06-start-payment.bru` |

## 10. 원본 근거와 추가 읽기

- [ASAK-back 구현 계획](../../IMPLEMENTATION_PLAN.md)
- [현재 소스의 공통 응답](../../src/main/java/com/asak/common/response/ApiResponse.java)
- [Bruno 컬렉션 사용 규칙](../../api/README.md)
- [정본 계약 결정](../../../ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md)
- [DevCopilot API 정리 기준](../../../ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md)
- [주문 API 계약](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/order/ORDER_API_CONTRACT.md)
- [결제 API 계약](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/payment/PAYMENT_API_CONTRACT.md)
- [주문 상태 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/03-order/ORDER_STATUS_IMPLEMENTATION.md)
- [관리자 주문 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/05-admin/ADMIN_ORDER_IMPLEMENTATION.md)
- [품절 관리 계약](../../../ASAK/docs/product_bible/03_Menu_Inventory_SoldOut/sold-out/SOLD_OUT_MANAGEMENT.md)
- [매출 API 계약](../../../ASAK/docs/product_bible/04_Dashboard_Sales_Kitchen_TTS/sales/SALES_API_CONTRACT.md)
- [취소·환불 및 매출 규칙](../../../ASAK/docs/product_bible/04_Dashboard_Sales_Kitchen_TTS/sales/SALES_CANCELLATION_REFUND_RULES.md)
