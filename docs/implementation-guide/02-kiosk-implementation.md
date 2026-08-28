# Kiosk 백엔드 API 작업 카드

> 현재 상태: 모든 항목은 계약/Bruno 요청만 존재하며 Controller HTTP 매핑은 미구현이다.

## 구현 순서

메뉴 조회 → 장바구니 재검증 → 주문 생성 → 결제수단 조회 → 결제 승인의 순서를 지킨다. 주문과 결제는 메뉴·옵션·가격을 DB 기준으로 확인할 수 있을 때만 구현한다.

| API | 경로 | Service 핵심 책임 | 필수 실패 사례 |
| --- | --- | --- | --- |
| API-001 | `GET /api/kiosk/categories` | 노출 카테고리를 `sortOrder` 순으로 조회 | 데이터 없음 |
| API-002 | `GET /api/kiosk/menuList` | 카테고리와 전체 메뉴 목록을 함께 조회하고, `menus[]` 카드 DTO(`menuId`, `categoryId`, `name`, `price`, `imageUrl`, `kcal`, `isSoldOut`, `isOrderable`) 조립 | 빈 목록 |
| API-003 | `GET /api/kiosk/menuDetail/{menuId}` | 메뉴 헤더(`baseKcal` 포함), `ingredients[]`, `optionGroups[].items[]`, `tags[]`를 상세 DTO로 조립 | `MENU_NOT_FOUND` 404 |
| API-004 | `POST /api/kiosk/cart/validate` | 수량·필수 옵션·품절·서버 가격 재검증 | 400 옵션 오류, 409 품절/가격 변경 |
| API-005 | `POST /api/kiosk/orders` | 서버 금액 계산, orderNo 생성, 주문·아이템·옵션 저장 | 400 요청 오류, 409 품절/가격 변경 |
| API-014 | `GET /api/kiosk/payment-methods` | 키오스크에 노출 가능한 결제수단만 반환 | 빈 목록 |
| API-006 | `POST /api/kiosk/payments` | 결제수단 활성화, 멱등성, 승인/실패 상태와 일별 고정 대기번호 처리 | 400 입력, 409 이미 승인/수단 비활성 |

## 주문 생성 request 기준

```json
{
  "orderType": "TAKE_OUT",
  "items": [
    {
      "menuId": 1,
      "quantity": 1,
      "optionItems": [{"optionItemId": 10, "quantity": 1}],
      "excludedIngredientIds": [3]
    }
  ]
}
```

- `orderType`, `items[].menuId`, `quantity`, `optionItems`, `excludedIngredientIds`가 계약 필드다.
- 클라이언트의 `totalAmount`나 카드 가격은 신뢰하지 않는다.
- API-005 성공 data는 `orderId`, `orderNo`, 서버 계산 `totalAmount`, `orderStatus: READY`다. 결제 승인 성공 data는 `paymentId`, `paymentMethodCode`, `paymentStatus: APPROVED`, `orderStatus: RECEIVED`, `approvedAmount`, `approvedAt`, `waitingOrderNo`를 반환한다. `waitingOrderNo`는 결제 완료 시 발급되어 `orders.waiting_order_no`에 저장되는 일별 고정 대기번호다.
- 결제 실패는 Order를 즉시 삭제하지 않는다. 승인 전 실패하면 주문은 `READY`로 남아 재시도할 수 있다.
- 클라이언트 금액과 DB 재계산 값이 다르면 주문을 중단하고 최신 가격/오류 code를 반환하여 Cart가 변경 안내를 할 수 있게 한다.

## Mapper/DB 확인 순서

1. 메뉴·카테고리·옵션 정책·품절 컬럼을 조회한다.
2. 메뉴 상세는 `menu_opt_policy → opt_policy → opt_policy_item → opt_item` 조인을 기준으로 설계한다.
3. 주문 저장은 주문 헤더 → 주문 아이템 → 선택 옵션/재료 제외 순으로 트랜잭션 처리한다.
4. 결제는 주문 상태와 결제수단 설정을 재확인하고 중복 승인 방지 키를 처리한다.
5. 결제 저장 후 한국 날짜의 `daily_waiting_sequence`를 증가시키고, 발급 번호와 날짜를 주문의 `waiting_order_no`, `waiting_date`에 저장하면서 `READY → RECEIVED` 상태를 한 번에 변경한다.

## 완료 조건

- Bruno에서 2xx뿐 아니라 400/404/409 response envelope까지 확인한다.
- 메뉴 가격·품절·필수 옵션은 DB 변경 뒤에도 다시 검증된다.
- 요청/응답 DTO, Mapper XML, Service 테스트가 한 API 단위로 함께 존재한다.

## 정본 링크

- [백엔드 정본 참조 팩](12-canonical-reference-pack.md)
- [메뉴 API 계약](../../../ASAK/docs/product_bible/03_Menu_Inventory_SoldOut/menu/MENU_API_CONTRACT.md)
- [주문 아키텍처](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/order/ORDER_ARCHITECTURE.md)
- [주문 생성 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/03-order/ORDER_CREATE_IMPLEMENTATION.md)
- [결제 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/04-payment/PAYMENT_IMPLEMENTATION.md)
