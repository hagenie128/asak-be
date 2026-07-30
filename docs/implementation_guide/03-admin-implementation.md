# Admin 백엔드 API 작업 카드

> 현재 상태: API 계약과 Bruno 요청은 존재하지만 Admin Controller/Service/Mapper에는 실행 로직이 없다.

## 주문 운영

| API | 경로 | 핵심 책임 | 우선 오류 |
| --- | --- | --- | --- |
| API-021 | `GET /api/admin/orders/live` | 현재 처리 대상 주문과 대기 수 조회 | 빈 목록 |
| API-007 | `GET /api/admin/orders` | 페이지·상태·기간·주문유형 필터 목록 | 400 잘못된 query |
| API-022 | `GET /api/admin/orders/{orderId}` | 주문·아이템·옵션·결제 상세 조립 | 404 `ORDER_NOT_FOUND` |
| API-008 | `PATCH /api/admin/orders/{orderId}/status` | 허용 상태 전이와 동시 변경 충돌 처리 | 404, 409 상태 전이 충돌 |
| API-024 | `PATCH /api/admin/orders/{orderId}/cancel` | 주문 취소·승인 결제 환불·시각 저장 | 409 `ORDER_CANCEL_NOT_ALLOWED` |

`RECEIVED`, `PREPARING`에서만 취소할 수 있다. 승인 결제는 `REFUNDED`와 `refundedAt`을 기록하며 `approvedAt`은 보존한다.

## 메뉴·품절·결제수단

| API | 경로 | 핵심 책임 |
| --- | --- | --- |
| API-009/010 | `PATCH` / `GET /api/admin/soldOut` | 메뉴·재료·옵션 품절 상태 조회/변경, Kiosk 영향 확인 |
| API-011/023 | `GET /api/admin/menus`, `/{menuId}` | 목록/상세 DTO 및 검색·필터 |
| API-012/013 | `POST` / `PATCH /api/admin/menus` | 메뉴·재료·옵션 정책 검증과 트랜잭션 저장 |
| API-015/016 | `GET` / `PATCH /api/admin/payment-methods` | 결제수단 노출/활성 설정과 Kiosk 조회 반영 |

메뉴 삭제/활성화는 아직 정책 보류다. 실제 DB에 `menu.active`가 없으므로 새 API 필드로 가정하지 않는다.

Admin mock의 메뉴 목록에는 `isActive`가 있지만 이는 화면 mock 필드다. 실제 API에서 이를 제공하려면 판매 활성화 정책과 DB 근거를 먼저 합의한다.

## 대시보드·매출

| API | 경로 | 읽기 원본 |
| --- | --- | --- |
| API-020 | `GET /api/admin/dashboard` | 주문·대기 수·매출 요약 집계 |
| API-017 | `GET /api/admin/sales/daily` | `vw_sales_daily`, `vw_sales_hourly` |
| API-018 | `GET /api/admin/sales/summary` | 기간 KPI·인기 메뉴 집계 |
| API-019 | `GET /api/admin/sales/monthly` | 월별 집계 |

원결제 금액은 gross sales에 남기고, 취소/환불 금액은 별도 canceled amount로 집계한다. `netSalesAmount = grossSalesAmount - canceledAmount`이며 취소/환불 주문은 인기 메뉴 순위에서 제외한다.

## Admin mock 연결 시 주의

- Live 주문 카드는 `menus[]`/`tone` 같은 화면 전용 모양이고, 주문 목록·상세는 `items[]`/`optionItems[]` 모양이다. 하나의 response DTO/adapter로 억지 통합하지 않는다.
- Mock 결제수단은 카드·카카오·네이버·제로 4종을 표현하지만, 현재 백엔드 enum은 `CARD`, `KAKAO_PAY`, `NAVER_PAY`뿐이다. `zero` 지원 여부와 methodId 타입을 API 구현 전에 확정한다.
- Mock의 `totalPrice`, `CANCELLED`, `PAID`는 실제 API response에 복사하지 않고 adapter 경계에서 `totalAmount`, `CANCELED`, `APPROVED`로 정규화한다.

## 완료 조건

- 목록 API의 `PageResult` 형식, 0/1-base page 정책, 날짜 범위를 먼저 통일한다.
- 변경 API는 400 검증 오류와 409 상태 충돌을 Bruno에서 확인한다.
- 매출 API는 실제 뷰와 합계/환불 반영 값을 대조한다.

## 정본 링크

- [백엔드 정본 참조 팩](12-canonical-reference-pack.md)
- [관리자 주문 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/docs/12-backend-implementation/05-admin/ADMIN_ORDER_IMPLEMENTATION.md)
- [품절 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/docs/12-backend-implementation/05-admin/SOLD_OUT_IMPLEMENTATION.md)
- [매출 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/docs/12-backend-implementation/06-sales/SALES_IMPLEMENTATION.md)
- [Admin mock 필드 사전](../../../ASAK-Admin/public/mocks/README.md)
