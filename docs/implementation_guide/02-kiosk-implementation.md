# Kiosk 화면 작업 카드

> `05-C Kiosk`의 **9개 Screen ID**를 구현할 때 바로 쓰는 문서다.  
> 각 카드에는 화면 행동, 필요한 데이터/API, 상태, 완료 확인만 둔다.

**Figma 공통 링크:** [05-C Screens / Kiosk](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-7720)

## SCR-001 · Home

**Route:** `/` · **목적:** 주문을 시작하고 주문 유형을 선택한다.

| 사용자 행동 | 화면 처리 | 데이터/API |
| --- | --- | --- |
| 주문 시작 | 주문 유형 선택 상태를 연다. | `EAT_IN` / `TAKE_OUT`은 주문 생성 전 임시 선택값이다. |
| 주문 유형 선택 | `/menu`로 이동하며 선택값을 주문 draft에 보관한다. | 이 화면 자체의 API는 정본에 정의되지 않았다. |
| 접근성 모드 진입 | `/accessibility`로 이동한다. | 현재 모드 값은 로컬 UI 상태다. |

**상태:** 기본, 주문 유형 선택, High Contrast.  
**완료 체크:** [ ] 두 주문 유형이 다음 화면까지 유지된다. [ ] 선택하지 않은 주문은 만들지 않는다. [ ] 접근성 모드 진입이 보인다.

## SCR-003 · Menu List

**Route:** `/menu` · **목적:** 메뉴를 찾고 상세로 이동하거나 장바구니에 담는다.

| 호출 | 보낼 값 | 카드에서 쓸 `data` |
| --- | --- | --- |
| `GET /api/kiosk/menuList` | query `categoryCode`, `tag`, `keyword` | `menuId`, `menuName`, `price`, `calories`, `imageUrl`, `isActive`, `isSoldOut`, `tags[]` |

| 상태/행동 | 처리 |
| --- | --- |
| 카테고리·태그·검색 | query 또는 목록 filter에 반영하고, 결과가 없으면 Empty를 보인다. |
| 메뉴 카드 선택 | `/menu/:menuId`로 이동한다. |
| 품절/비활성 메뉴 | 주문 진입을 막고 카드 상태를 보인다. |
| Loading / Error | 이전 목록을 정상 결과처럼 두지 않고 loading·재시도를 보인다. |
| 장바구니 담기 toast / 빈 장바구니 | 실제 장바구니 상태에 맞게 toast·empty를 보인다. |

**완료 체크:** [ ] 필터와 `menuId`가 연결된다. [ ] 품절 카드는 주문되지 않는다. [ ] Loading/Empty/Error/카테고리 비활성 상태를 확인했다.

## SCR-004 · Menu Detail

**Route:** `/menu/:menuId` · **목적:** 옵션·재료 제외·수량을 고르고 장바구니 항목을 만든다.

| 호출 | 보낼 값 | 상세에서 쓸 `data` |
| --- | --- | --- |
| `GET /api/kiosk/menuDetail/{menuId}` | path `menuId` | `basePrice`, `ingredients[]`, `allergens[]`, `optionGroups[]`, `isSoldOut` |

| 화면 요소 | 응답 필드 | 처리 |
| --- | --- | --- |
| 재료 제거 | `ingredients[].ingredientId`, `canRemove`, `isSoldOut` | 제거 가능 재료만 제외 목록에 넣고, 품절 상태를 보인다. |
| 옵션 선택 | `isRequired`, `minimumSelection`, `maximumSelection`, `options[]` | 최소/최대 검증을 하고 품절 옵션은 선택하지 못하게 한다. |
| 금액 | `basePrice`, `options[].additionalAmount`, 수량 | 화면 합계는 미리 보이되 주문 생성 서버값이 최종이다. |
| 알레르기 | `allergens[].displayName` | 펼침/접힘 상태에서도 확인 가능해야 한다. |

**상태:** 옵션 선택, Loading, Error, 알레르기 펼침, 메뉴·재료·베이스·옵션 품절, 수량 제한 toast, 장바구니 수정/저장/저장 오류/취소.  
**완료 체크:** [ ] draft 수정 취소가 원본 장바구니를 바꾸지 않는다. [ ] 필수 옵션/수량 제한이 작동한다. [ ] 품절 항목이 담기지 않는다.

<details>
<summary>상세 응답 예시</summary>

```json
{"data":{"menuId":1,"menuName":"멕시칸 랩","basePrice":7200,"calories":430,"isSoldOut":false,"ingredients":[{"ingredientId":33,"ingredientName":"양파","canRemove":true,"isSoldOut":false}],"optionGroups":[{"optionGroupId":10,"optionGroupName":"드레싱","isRequired":true,"minimumSelection":1,"maximumSelection":1,"options":[{"optionItemId":101,"additionalAmount":0,"isSoldOut":false}]}],"allergens":[{"allergenCode":"MILK","displayName":"우유"}]}}
```
</details>

## SCR-005 · Cart

**Route:** `/cart` · **목적:** 주문 항목을 검토·수정하고 주문 생성을 시작한다.

| 호출 | 보낼 값 | 성공 뒤 사용할 `data` |
| --- | --- | --- |
| `POST /api/kiosk/orders` | `orderType`, `items[].menuId`, `quantity`, `selectedOptionItemIds`, `excludedIngredientIds` | `orderId`, `orderNo`, `orderStatus`, `paymentStatus`, `totalAmount`, `items[].lineAmount` |

| 상태/행동 | 처리 |
| --- | --- |
| 삭제/마지막 항목 삭제/전체 비우기 | 확인 상태 뒤 삭제하고 마지막이면 Empty로 전환한다. |
| 옵션 수정 | SCR-004의 draft 편집으로 이동하고 성공할 때만 항목을 교체한다. |
| 주문 생성 중 | 결제하기 버튼을 잠그고 요청을 하나만 보낸다. |
| `ORDER_PRICE_CHANGED` | 장바구니를 지우지 않고 서버의 가격 변경을 알린다. |
| `MENU_SOLD_OUT`, `OPTION_ITEM_SOLD_OUT` | 문제 항목을 표시하고 수정·삭제한 뒤 다시 시도한다. |
| 주문 생성 성공 | `orderId`와 서버 `totalAmount`를 `/payment`에 넘긴다. |

```json
{"orderType":"EAT_IN","items":[{"menuId":1,"quantity":2,"selectedOptionItemIds":[101,104],"excludedIngredientIds":[33]}]}
```

**완료 체크:** [ ] 빈 장바구니 결제가 불가하다. [ ] 실패 후 장바구니가 남는다. [ ] 화면 합계와 서버 `totalAmount`가 다르면 서버값을 따른다.

## SCR-007 · Payment

**Route:** `/payment` · **목적:** 주문에 결제 수단을 적용하고 승인 요청을 한 번만 보낸다.

| 호출 | 보낼 값 | 화면에서 쓸 `data` |
| --- | --- | --- |
| `GET /api/kiosk/paymentMethods` | 없음 | `paymentMethodCode`, `displayName`, `status`, `sortOrder` |
| `POST /api/kiosk/payments` | `orderId`, `paymentMethodCode`, `idempotencyKey` | `orderNo`, `approvedAmount`, `approvedAt`, `waitingOrderCount` |

| 상태/행동 | 처리 |
| --- | --- |
| `ENABLED` / `DISABLED` / `MAINTENANCE` | 선택 가능 / 비활성 / 점검 안내로 구분한다. |
| 모든 수단 비활성 / Load Error | 결제 진행을 막고 원인과 복귀 행동을 보인다. |
| Processing | 뒤로가기·연타를 막고 새 결제 요청을 만들지 않는다. |
| 승인 | SCR-008로 이동한다. |

```json
{"orderId":128,"paymentMethodCode":"CARD","idempotencyKey":"uuid"}
```

**완료 체크:** [ ] 주문 없이 결제 진입하지 않는다. [ ] 연타해도 결제 요청은 하나다. [ ] 결제 수단 상태가 Admin 설정과 맞는다.

## SCR-008 · Complete

**Route:** `/complete` · **목적:** 승인된 주문 결과와 대기 정보를 보여 주고 새 주문으로 안전하게 끝낸다.

| 보여 줄 값 | 출처 |
| --- | --- |
| 주문 번호 | 결제 승인 `orderNo` |
| 승인 금액·시각 | `approvedAmount`, `approvedAt` |
| 현재 대기 수 | `waitingOrderCount` |

| 행동 | 처리 |
| --- | --- |
| 새 주문 | 주문/결제 draft를 초기화한 뒤 홈으로 이동한다. |
| 뒤로 가기 | 이전 결제 화면에서 재결제되지 않도록 history를 교체한다. |

**완료 체크:** [ ] 승인 응답이 없는 완료 화면을 만들지 않는다. [ ] 새 주문 뒤 이전 결제 정보가 남지 않는다.

## SCR-012 · Payment Error

**Route:** 결제 화면의 상태/overlay · **목적:** 실패 이유와 안전한 다음 행동을 제공한다.

| 실패 값 | 처리 |
| --- | --- |
| `failureCode: CARD_DECLINED` | 거절 사실을 알리고 다른 수단/재시도를 제공한다. |
| `canRetry: true` | 같은 주문으로 재시도할 수 있게 한다. |
| network 실패 | 주문·장바구니를 보존하고 재시도/장바구니 복귀를 제공한다. |
| `PAYMENT_IN_PROGRESS`, `PAYMENT_ALREADY_APPROVED` | 새 결제를 만들지 않고 현재 결과를 확인한다. |

**완료 체크:** [ ] 실패가 성공 화면처럼 보이지 않는다. [ ] 실패 뒤 주문과 장바구니가 사라지지 않는다. [ ] retry 불가면 재시도 버튼을 주지 않는다.

## SCR-013 · Timeout

**Route:** 상태/overlay · **목적:** 장시간 미조작 주문을 안전하게 경고·복구한다.

| 상태/행동 | 처리 |
| --- | --- |
| 경고 카운트다운 | 남은 시간을 보이고 `계속 주문` 행동을 준다. |
| 계속 주문 | 안전하게 세션을 연장하고 원래 화면으로 돌아간다. |
| 결제 Processing | timeout 때문에 결제 상태를 초기화하거나 새 결제를 만들지 않는다. |

**완료 체크:** [ ] 경고/계속 주문/복귀가 보인다. [ ] 결제 중 timeout이 중복 결제를 만들지 않는다.

## SCR-014 · Accessibility

**Route:** `/accessibility` · **목적:** High Contrast 등 접근성 모드에서도 동일한 주문 흐름을 제공한다.

| 행동 | 처리 |
| --- | --- |
| 모드 선택 | 현재 모드를 명확히 보이고 일반 모드로 되돌릴 수 있게 한다. |
| 주문 진행 | 메뉴 선택·옵션·장바구니·결제가 일반 모드와 같은 순서로 동작한다. |
| Disabled/Error | 색만으로 상태를 전달하지 않는다. |

**완료 체크:** [ ] High Contrast/Reverted 상태를 확인했다. [ ] 접근성 모드에서 전체 주문을 끝낼 수 있다.

<details>
<summary>원본 문서가 필요할 때만 열기</summary>

- [Home/Menu Frontend Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/03-kiosk/HOME_MENU_IMPLEMENTATION.md)
- [Cart Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/03-kiosk/CART_IMPLEMENTATION.md)
- [Payment/Complete Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/03-kiosk/PAYMENT_COMPLETE_IMPLEMENTATION.md)
- [Timeout/Accessibility Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/03-kiosk/TIMEOUT_ACCESSIBILITY_IMPLEMENTATION.md)
- [Menu API Contract](../product_bible/03_Menu_Inventory_SoldOut/docs/09-features/menu/MENU_API_CONTRACT.md)
- [Order API Contract](../product_bible/02_Order_Cart_Payment/docs/09-features/order/ORDER_API_CONTRACT.md)
- [Payment API Contract](../product_bible/02_Order_Cart_Payment/docs/09-features/payment/PAYMENT_API_CONTRACT.md)
</details>
