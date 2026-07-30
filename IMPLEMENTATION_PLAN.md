# ASAK 백엔드 구현 계획

> 상태: 현재 합의 정본 (2026-07-23)
> 범위: API·DB·문서 계약 정렬. 이 문서는 기능 구현 완료를 의미하지 않는다.

## 1. 정본과 작업 위치

1. 백엔드 작업 저장소는 `ASAK-back`이며 원격 저장소는 `nayeon0828/ASAK-backend`다.
2. Product Bible, Canonical, 화면 계약, DevCopilot API 명세를 함께 확인한다. 서로 충돌하면 최신 합의 문서와 실제 DB를 우선한다.
3. 이전 `/api/menus`, `/api/orders`, `/api/payments`, `/api/v1/**`는 레거시 계약이므로 새 Controller에 복사하지 않는다.

## 2. 구조와 DB 접근 원칙

- 현재 `admin`, `user`, `common` 패키지는 유지한다. 구현 전에 전면 패키지 이동을 하지 않는다.
- Mapper는 MyBatis 인터페이스로 선언하고 XML은 `src/main/resources/mappers/**`에 둔다.
- 조회·필터·조인·집계는 Mapper XML의 SQL에서 처리한다.
- Service는 요청 검증, 트랜잭션, 금액 계산, 상태 전이를 담당한다.
- API 하나를 Controller → Service → Mapper → XML → DB → 응답 DTO 순서로 세로 완성한다.
- DTO는 기능별 `dto/request`, `dto/response` 패키지에 분리한다. 빈 `request.java`, `response.java` 클래스는 만들지 않는다.

## 3. 공통 응답과 필드 규칙

모든 새 API는 아래 응답 형식을 사용한다.

```json
{
  "success": true,
  "status": 200,
  "code": "MENU_LIST_SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

`code`는 API별 의미 있는 문자열이다. (예: `MENU_LIST_SUCCESS`, `ORDER_CREATE_SUCCESS`)  
공통 헬퍼 기본값은 `OK`이며, 레거시 숫자 코드(`0000`, `2001`)는 쓰지 않는다.

### API 필드와 DB 매핑

- Java/JSON은 camelCase를 쓴다.
- `orders.total_price` → `totalAmount`
- `payment.amount` → `approvedAmount`
- `payment.paid_at` → `approvedAt`
- `orders.canceled_at` → `canceledAt`
- `payment.refunded_at` → `refundedAt`
- `menu.cat_id` → `categoryId`
- `menu.sold_out` → `isSoldOut`
- `category.sort_no` → `sortOrder`
- `category.active` → `isActive`
- `category` 테이블에는 코드 컬럼이 없으므로 `categoryCode` 대신 `categoryId`를 사용한다.
- 주문 유형 enum은 `EAT_IN`, `TAKE_OUT`을 사용한다. API 요청도 `TAKE_OUT`으로 통일한다.
- 주문 상태 enum은 `RECEIVED`, `PREPARING`, `COMPLETED`, `CANCELED`을 사용한다.
- 결제 상태 enum은 `READY`, `APPROVED`, `FAILED`, `CANCELED`, `REFUNDED`를 사용한다.
- 결제수단 enum은 `CARD`, `KAKAO_PAY`, `NAVER_PAY`를 사용한다. 삼성페이는 카드 단말 결제이므로 별도 코드가 아니라 `CARD`에 포함한다.

## 4. API 정본 목록

### 키오스크

| API | Method | 경로 | 비고 |
| --- | --- | --- | --- |
| API-001 | GET | `/api/kiosk/categories` | 카테고리 탭 목록 |
| API-002 | GET | `/api/kiosk/menuList` | 카테고리·메뉴 카드 목록 |
| API-003 | GET | `/api/kiosk/menuDetail/{menuId}` | 재료·알레르기·옵션·품절 정보 포함 |
| API-004 | POST | `/api/kiosk/cart/validate` | 주문 직전 가격·필수 옵션·품절 재검증 |
| API-005 | POST | `/api/kiosk/orders` | 주문 생성, 서버가 `totalAmount` 계산 |
| API-006 | POST | `/api/kiosk/payments` | 가상 결제 승인 |
| API-014 | GET | `/api/kiosk/payment-methods` | 키오스크 결제수단 목록 |

주문 요청은 `orderType`, `items[].menuId`, `quantity`, `optionItems[].optionItemId`, `optionItems[].quantity`, `excludedIngredientIds`를 사용한다. 현재 DTO와 Bruno도 이 모양을 따른다. 클라이언트가 결제 금액을 정본으로 보내지 않는다.

### 관리자

| API | Method | 경로 | 요구사항 |
| --- | --- | --- | --- |
| API-007 | GET | `/api/admin/orders` | LMIS-ORDER-001 |
| API-008 | PATCH | `/api/admin/orders/{orderId}/status` | LMIS-ORDER-003 |
| API-009 / 010 | PATCH / GET | `/api/admin/soldOut` | LMIS-MENU-001 |
| API-011 | GET | `/api/admin/menus` | LMIS-MENU-004 |
| API-012 / 013 | POST / PATCH | `/api/admin/menus`, `/api/admin/menus/{menuId}` | LMIS-MENU-004 |
| API-015 / 016 | GET / PATCH | `/api/admin/payment-methods`, `/api/admin/payment-methods/{methodId}` | LMIS-PAY-001 |
| API-017 / 018 / 019 | GET | `/api/admin/sales/daily`, `/summary`, `/monthly` | LMIS-ORDER-005 |
| API-020 | GET | `/api/admin/dashboard` | LMIS-DASH-001 |
| API-021 | GET | `/api/admin/orders/live` | LMIS-ORDER-001 |
| API-022 | GET | `/api/admin/orders/{orderId}` | LMIS-ORDER-002 |
| API-023 | GET | `/api/admin/menus/{menuId}` | LMIS-MENU-004 |
| API-024 | PATCH | `/api/admin/orders/{orderId}/cancel` | DEV-ORDER-002 |

### API-024 주문 취소·환불 규칙

1. `RECEIVED`, `PREPARING` 상태에서만 취소할 수 있다.
2. 주문 상태를 `CANCELED`로 변경하고 `orders.canceled_at`을 저장한다.
3. 결제가 승인된 주문은 `payment.paid_at`을 유지한다.
4. 승인 결제는 결제 상태를 `REFUNDED`로 변경하고 `payment.refunded_at`을 저장한다.
5. 완료 또는 이미 취소된 주문은 `409 ORDER_CANCEL_NOT_ALLOWED`를 반환한다.

## 5. 실제 DB와 매출 뷰 규칙

- 실제 옵션 경로는 `menu_opt_policy → opt_policy → opt_policy_item → opt_item`이다. `menu_option`은 레거시 이름이다.
- `item_exclusion`, `order_item`, `order_item_option`은 재료 제외와 선택 옵션을 저장한다.
- `pay_method_cfg`, `common_code`는 결제수단과 상태 코드를 관리한다.
- `vw_payment_result`를 결제 승인 응답 읽기 원본으로 사용한다. (`paymentId`, `orderId`, `orderNo`, `paymentStatus`, `approvedAmount`, `approvedAt`, `waitingOrderCount`)
- 결제수단 목록·품절 영향 메뉴 수는 뷰가 아니라 매퍼 인라인 쿼리다. (`view.sql` [17]·[18])
- `vw_sales_daily`, `vw_sales_hourly`, `vw_top_menu_daily`, `vw_top_menu_hourly`를 매출 API의 읽기 원본으로 사용한다.
- `payment.paid_at`이 있는 원결제 금액은 gross sales에 유지한다.
- 주문 또는 결제가 취소·환불되면 해당 금액은 canceled amount로 별도 집계한다.
- `net_sales_amount = gross_sales_amount - canceled_amount`이며, 전액 환불은 순매출 0으로 계산되어야 한다.
- 인기 메뉴 뷰는 취소·환불 주문의 아이템을 수량·주문 수·매출·순위에서 제외한다.
- `menu.active`는 실제 DB에 없다. 메뉴 판매 활성화 정책은 `menu.sold_out`과 별도 합의가 필요하다.

## 6. 구현 순서

1. 공통 응답·예외 처리와 MyBatis 스캔/경로 설정
2. `GET /api/health`
3. 카테고리·메뉴 목록·메뉴 상세
4. 장바구니 검증과 주문 생성
5. 결제 승인
6. 관리자 활성 주문, 주문 목록·상세, 상태 변경
7. 주문 취소·환불
8. 품절 관리
9. 매출 뷰·대시보드
10. 메뉴 등록·수정

## 7. 현재 화면 경로

관리자 화면은 `/`, `/dashboard`, `/orders`, `/sold-out`, `/menus`, `/payment-methods`, `/sales`, `/sales/monthly`, `/sales/daily`를 사용한다.

키오스크 화면은 `/`, `/menu`, `/menu/:menuId`, `/cart`, `/payment`, `/complete`를 사용한다. `/paymentProcessing`은 결제 진행 상태용 구현 경로이며 별도 비즈니스 API가 아니다.

## 8. 보류

- 메뉴 삭제는 `menu` 테이블의 soft delete 또는 활성 상태 정책을 합의한 뒤 추가한다.
- 멤버십, 영수증 출력, QR/바코드, 접근성 API, WebSocket은 현재 구현 범위 밖이다.
