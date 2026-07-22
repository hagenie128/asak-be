# Admin 화면 작업 카드

> `06-C Admin`의 **10개 Screen ID**를 구현할 때 바로 쓰는 문서다.  
> 각 카드에는 화면 행동, 필요한 데이터/API, 상태, 완료 확인만 둔다.

**Figma 공통 링크:** [06-C Screens / Admin](https://www.figma.com/design/JSrjOy668zhfkiLplCkreh/ASAK-%E2%80%94-Design-System---Product-UI-0715?node-id=134-10606)

**Mock 필드·props 치트시트 (바인딩 시 여기부터):**  
[`ASAK-Admin/public/mocks/README.md`](../../../ASAK-Admin/public/mocks/README.md) · 각 Page/컴포넌트 파일 상단 주석 · `adminMockRepository.js` 헤더

## SCR-015 · Login

**Route:** `/login` · **목적:** 관리자가 인증된 상태로 Admin에 들어간다.

| 상태/행동 | 처리 | 데이터/API |
| --- | --- | --- |
| 아이디·비밀번호 입력 | 빈 값/형식 검증을 즉시 보인다. | 로그인 endpoint/DTO는 현재 정본 API 계약에서 확정되지 않았다. 임의로 만들지 않는다. |
| 제출 중 | 제출 버튼을 잠그고 중복 로그인을 막는다. | 인증 구현 시 token/세션 보관 위치를 팀에서 확정한다. |
| 인증 실패/권한 없음 | 비밀번호를 지우거나 오류 원인을 과도하게 노출하지 않는다. | `401`/`403`의 공통 표현은 [API 공통 규칙](04-api-db-implementation.md)을 따른다. |

**완료 체크:** [ ] Default/validation/auth error/submitting이 06-C와 맞다. [ ] 로그인 성공 전 보호 route에 진입하지 않는다.

## SCR-022 · Dashboard

**Route:** `/` · **목적:** 지금 운영 상황을 한 화면에서 판단한다.

| 호출 | 보낼 값 | 화면에서 쓸 `data` |
| --- | --- | --- |
| `GET /api/admin/dashboard` | 기간/집계 조건은 Dashboard 원본 계약 확인 | 현재 매출·주문·대기·품절 등 Dashboard summary 값 |

| 상태/행동 | 처리 |
| --- | --- |
| Loading | 이전 지표를 현재값처럼 보이지 않게 한다. |
| Empty | 운영 데이터가 아직 없음을 알려 준다. |
| Error | 화면 전체가 0처럼 보이지 않게 오류와 재시도를 보인다. |
| Partial Data | 누락된 카드만 미확정으로 표현한다. |

**완료 체크:** [ ] Default/Loading/Empty/Error/Partial Data를 구분한다. [ ] Dashboard 값과 매출·주문 관리의 금액/상태 기준이 같다.

## SCR-009 · Live Order Board

**Route:** `/orders/live` · **목적:** 들어온 주문을 보고 조리 상태를 안전하게 바꾼다.

| 호출 | 보낼 값 | 화면에서 쓸 `data` |
| --- | --- | --- |
| 주문 상세 `GET /api/admin/orders/{orderId}` | path `orderId` | `orderId`, `orderNo`, `orderType`, `orderStatus`, `paymentStatus`, `totalAmount`, `createdAt`, `items` |
| 상태 변경 `PATCH /api/admin/orders/{orderId}/status` | `{"status":"COMPLETED"}` | `previousStatus`, `status`, `updatedAt` |

| 상태/행동 | 처리 |
| --- | --- |
| 새 주문 알림 | 새 주문이 보이되 알림 실패가 주문 누락처럼 보이면 안 된다. |
| 상세 열기 | 목록 요약으로 없는 옵션/제외 재료는 상세 데이터에서 확인한다. |
| 상태 변경 확인·저장 중 | 확인 뒤 요청하고 같은 요청을 잠근다. |
| 성공 | 응답을 받은 뒤에만 카드 상태와 TTS를 갱신한다. |
| `409` | 최신 주문을 다시 읽고 임의로 덮어쓰지 않는다. |
| TTS 실패 | 주문 상태는 성공대로 유지하고 TTS 실패만 별도로 표시한다. |

`RECEIVED → PREPARING → COMPLETED`가 MVP 상태 전이다.  
**완료 체크:** [ ] Loading/Empty/Error/상세/확인/저장/성공/실패/TTS 실패가 있다. [ ] 완료 주문을 다시 완료해도 TTS/매출이 중복되지 않는다.

## SCR-010 · Order Management

**Route:** `/orders` · **목적:** 주문을 검색·필터·상세 확인한다.

| 필요한 데이터 | 사용처 |
| --- | --- |
| `orderId`, `orderNo`, `orderType` | 목록 식별/주문 유형 |
| `orderStatus`, `paymentStatus` | 상태 필터/상태 표시 |
| `totalAmount`, `createdAt` | 금액/기간/정렬 |
| `items`, `selectedOptions`, `excludedIngredients` | 상세 확인 |

| 상태/행동 | 처리 |
| --- | --- |
| 필터 적용 | 필터값을 유지한 채 목록을 다시 읽는다. |
| 상세 열기 | 선택한 주문의 상세를 보이고 목록의 선택 상태를 유지한다. |
| Loading / Empty / Error | 데이터 없음과 조회 실패를 다르게 보인다. |

**API 메모:** 목록 `GET /api/admin/orders`의 query/DTO는 Draft를 확정해야 한다. 상세와 상태 변경은 위 SCR-009 계약을 사용한다.  
**완료 체크:** [ ] 필터/상세/Loading/Empty/Error가 있다. [ ] 목록·상세·Dashboard가 같은 상태/금액 정의를 쓴다.

## SCR-011 · Sold-out Management

**Route:** `/soldOut` · **목적:** 메뉴·재료·옵션의 품절을 변경하고 Kiosk 영향까지 관리한다.

| 호출 | 보낼 값 | 응답/목록에서 쓸 값 |
| --- | --- | --- |
| `PATCH /api/admin/soldOut` | `targetType`, `targetId`, `isSoldOut` | `targetType`, `targetId`, `name`, `isSoldOut`, `reasonType` |

```json
{"targetType":"OPTION_ITEM","targetId":101,"isSoldOut":true}
```

| 상태/행동 | 처리 |
| --- | --- |
| 항목 변경/저장 중 | 해당 토글을 잠그고 중복 저장하지 않는다. |
| 실패 | 성공 전 값으로 되돌리고 오류를 보인다. |
| 전체 비활성 | 확인 상태를 보이고 Kiosk 결제/주문 영향도 알린다. |
| `MENU`/`INGREDIENT`/`OPTION_ITEM` | 영향 범위를 boolean 하나로 축소하지 않는다. |

**완료 체크:** [ ] Loading/Empty/Error/변경/확인/저장/성공/실패 상태가 있다. [ ] 변경이 Kiosk 목록·상세·장바구니·주문 생성까지 일관된다.

## SCR-016 · Menu Management

**Route:** `/menus` · **목적:** 메뉴를 추가·수정·삭제하고 재료/옵션 구성을 관리한다.

| 호출 | 보낼 값 | 화면에서 쓸 값 |
| --- | --- | --- |
| `POST /api/admin/menus` | `menuName`, `description`, `categoryCode`, `basePrice`, `imageUrl`, `isActive`, `tagCodes`, `ingredients[]`, `optionGroups[]` | 저장한 메뉴 기본 정보와 구성 |
| `PATCH /api/admin/menus/{menuId}` | 위 값과 path `menuId` | 수정 결과 |
| `GET /api/admin/ingredients` | `keyword`, `categoryCode`, `page` | 메뉴 구성에 넣을 재료 |
| `POST /api/admin/menuImages` | 이미지 파일 | `imageUrl` |
| `POST /api/admin/menus/nutrition/calculate` | 메뉴/재료 구성 | 영양 계산 결과 |

| 상태/행동 | 처리 |
| --- | --- |
| 추가/수정 상세 | form draft에서 편집하고, 저장 성공 전 목록값을 바꾸지 않는다. |
| validation error | 어떤 입력이 필요한지 해당 필드 가까이에 보인다. |
| 삭제 | 확인을 거친다. |
| 저장 중/성공/실패 | 입력을 잠그고, 실패하면 입력을 보존한다. |

**완료 체크:** [ ] Default/추가/수정/검증/삭제/저장/성공/실패/Loading/Empty를 비교했다. [ ] 옵션/재료 품절 규칙을 Kiosk와 맞췄다.

## SCR-018 · Payment Method Settings

**Route:** `/paymentMethods` · **목적:** Kiosk 결제 수단의 노출·순서·사용 가능 상태를 관리한다.

| 호출 | 보낼 값 | 화면에서 쓸 값 |
| --- | --- | --- |
| `GET /api/admin/paymentMethods` | 없음 | `paymentMethodId`, `displayName`, `status`, `sortOrder`, `receiptMessage` |
| `PATCH /api/admin/paymentMethods/{paymentMethodId}` | `status`, `sortOrder`, `receiptMessage`, `failureRetentionMinutes` | 변경된 결제 수단 |

| 상태/행동 | 처리 |
| --- | --- |
| 수단 변경 | 저장 전 변경값을 확인한다. |
| 전체 비활성 | Kiosk가 결제할 수 없음을 경고하고 확인을 받는다. |
| 저장 중/성공/실패 | 요청 중 잠그고, 실패 시 이전 값으로 복구한다. |

**완료 체크:** [ ] Default/변경/확인/저장/성공/실패/전체 비활성/Loading/Error가 있다. [ ] Kiosk `ENABLED`/`DISABLED`/`MAINTENANCE` 표현과 맞다.

## SCR-019 · Sales Summary

**Route:** `/sales` · **목적:** 선택한 기간의 매출 요약을 확인한다.

| 호출 | 보낼 값 | 화면에서 쓸 `data` |
| --- | --- | --- |
| `GET /api/admin/sales/summary` | `startDate`, `endDate` | `period`, `kpis`, `dailyTrend`, `hourlyTrend`, `popularMenus`, `orderTypeRatio` |

| 상태/행동 | 처리 |
| --- | --- |
| 기간 변경 | 이전 기간 데이터와 Loading을 섞지 않는다. |
| Empty / Error | 기간 내 주문 없음과 조회 실패를 구분한다. |
| 금액/비율 | 금액은 integer, 비율은 `0~1`, 날짜는 `YYYY-MM-DD`로 처리한다. |

**완료 체크:** [ ] Default/필터/Loading/Empty/Error가 있다. [ ] 근거 없는 할인·환불/KPI를 실데이터처럼 보이지 않는다.

## SCR-020 · Monthly Sales

**Route:** `/sales/monthly` · **목적:** 선택한 연도의 월 단위 매출을 본다.

| 호출 | 보낼 값 | 화면에서 쓸 값 |
| --- | --- | --- |
| `GET /api/admin/sales/monthly` | `year` | 월 단위 매출·주문 집계와 비교값 |

| 상태/행동 | 처리 |
| --- | --- |
| 연도 변경 | 선택 연도와 응답 기간이 맞는지 확인한다. |
| Loading / Empty / Error | 06-C의 세 상태를 구분한다. |

**완료 체크:** [ ] 연도 변경이 데이터/차트/표 모두에 반영된다. [ ] SCR-019와 금액 정의가 같다.

## SCR-021 · Daily Sales

**Route:** `/sales/daily` · **목적:** 선택한 하루의 시간대별 매출을 본다.

| 호출 | 보낼 값 | 화면에서 쓸 값 |
| --- | --- | --- |
| `GET /api/admin/sales/daily` | `date` | 시간대별 매출·주문 집계와 일자 요약 |

| 상태/행동 | 처리 |
| --- | --- |
| 날짜 변경 | `YYYY-MM-DD`, `Asia/Seoul` 기준으로 다시 조회한다. |
| Loading / Empty / Error | 선택한 일자 데이터 없음과 실패를 구분한다. |

**완료 체크:** [ ] 날짜 변경이 모든 지표에 반영된다. [ ] 시간대가 중복/누락 없이 정렬된다.

<details>
<summary>원본 문서가 필요할 때만 열기</summary>

- [Dashboard Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/DASHBOARD_IMPLEMENTATION.md)
- [Live Order/TTS Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/LIVE_ORDER_TTS_IMPLEMENTATION.md)
- [Order Management Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/ORDER_MANAGEMENT_IMPLEMENTATION.md)
- [Sold-out Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/SOLD_OUT_IMPLEMENTATION.md)
- [Menu Management Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/MENU_MANAGEMENT_IMPLEMENTATION.md)
- [Sales Guide](../product_bible/12_Frontend_Implementation/docs/13-frontend-implementation/04-admin/SALES_IMPLEMENTATION.md)
- [Sales API Contract](../product_bible/04_Dashboard_Sales_Kitchen_TTS/docs/09-features/sales/SALES_API_CONTRACT.md)
</details>
