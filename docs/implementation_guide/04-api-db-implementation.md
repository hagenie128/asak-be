# API·DB 구현 규칙

> 상태: Current · 기준일: 2026-07-23

## 실제 연결 상태

| 항목 | 현재 상태 | 작업 시 주의 |
| --- | --- | --- |
| `ApiResponse<T>` | 필드 구조 존재 | 성공/실패 factory와 Controller 적용은 아직 필요 |
| Controller/Service/Mapper | 패키지와 빈 클래스 존재 | 매핑 annotation, Service 로직, SQL은 아직 없음 |
| Bruno `api/` | 목표 계약 요청 24개 존재 | `SPEC_ONLY`; 구현 전 성공 응답을 기대하지 않음 |
| MyBatis | 의존성·mapper-locations 설정 존재 | Mapper scan, SQL, 결과 DTO 매핑은 API별 확인 필요 |
| DB 설정 | 외부 MySQL 접속 정보와 `ddl-auto=none` 존재 | 스키마를 코드가 자동 변경하지 않으며, 실제 컬럼 확인이 선행 |

## 목표 API 범위

| 영역 | API |
| --- | --- |
| Kiosk | categories, menuList, menuDetail, cart validate, orders, payments, payment methods |
| Admin 주문 | active orders, list, detail, status update, cancel/refund |
| Admin 메뉴 | menu list/detail/create/update, sold-out |
| Admin 운영 | payment methods, dashboard, sales daily/summary/monthly |

정확한 URL과 API 번호는 [기능 구현 매트릭스](08-feature-implementation-matrix.md) 및 `api/` Bruno 요청을 기준으로 한다.

## 공통 응답·오류의 기존 정의

이 항목은 새 정책이 아니라 다음 정본을 구현 단계에 맞춰 연결한 것이다.

- [DevCopilot API 정리 기준](../../../ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md): 이 저장소에 적용할 `{ success, status, code, message, data }` 계약과 현재 API 목록
- [예외 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/01-common/EXCEPTION_IMPLEMENTATION.md): `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`의 구현 골격
- [검증·예외 규칙](../../../ASAK/docs/product_bible/06_Engineering_Bible/03-backend/VALIDATION_AND_EXCEPTION_RULES.md): Bean Validation/Service 검증/DB 제약의 역할과 400·404·409 기준

현재 `ApiResponse`, `GlobalExceptionHandler`, `ErrorCode`는 소스에 클래스 또는 필드 골격만 있으므로, 관리자 API를 만들기 전에 위 세 문서를 기준으로 공통 기반을 먼저 완성한다. `API_DESIGN_RULES.md`의 3필드 예시는 2026-07-23 정렬 문서의 5필드 계약과 다르므로, 구현 시에는 후자를 적용하고 차이를 남긴다.

### 업무 코드 규칙

- API의 `code`는 문자열이지만 값은 `"0000"`, `"1001"`, `"2001"`처럼 고유 숫자 코드로 반환한다.
- HTTP `status`는 전송 결과(`400`/`404`/`409` 등), 업무 `code`는 프론트가 오류를 구분하는 값이다.
- 현재 공통 매핑은 옵션 `1001`, 메뉴 `2001`~`2002`, 주문 `3001`~`3003`, 결제 `4001`~`4003`이다. 상세 목록은 `ErrorCode.java`를 정본으로 한다.

## 필드·DB 매핑

| DB | API DTO | 비고 |
| --- | --- | --- |
| `orders.total_price` | `totalAmount` | 요청 금액이 아닌 서버 계산 결과 |
| `payment.amount` | `approvedAmount` | 승인/환불 규칙과 함께 처리 |
| `payment.paid_at` | `approvedAt` | 승인 시각 |
| `orders.canceled_at` | `canceledAt` | 주문 취소 시각 |
| `payment.refunded_at` | `refundedAt` | 승인 결제 환불 시각 |
| `menu.cat_id` | `categoryId` | `categoryCode` 사용 금지 |
| `menu.sold_out` | `isSoldOut` | 메뉴 판매 가능 여부 |
| `category.sort_no` | `sortOrder` | 탭/목록 정렬 |
| `category.active` | `isActive` | 카테고리 노출 여부 |
| `payment.idempotency_key` | `idempotencyKey` | 결제 승인 요청 필드. 저장 후 UNIQUE로 중복 결제 차단 |
| `menu.image_asset_id` | `mediaAssetId` | `media_asset` FK. 조회 URL은 `media_asset.url` |

## 실제 DB 정책

> 컬럼·기본값·제약조건의 정본은 [`docs/아삭_mysql.sql`](../아삭_mysql.sql)이다. 운영 DB 실측본이며
> [`docs/tools/schema_sync.py`](../tools/README.md)로 재생성·검증한다. 문서와 실제가 어긋난 적이 있으므로
> 컬럼을 기억이나 옛 문서로 가정하지 말 것 (2026-08-19 대조 내역: [`docs/2026-08-19_schema_doc_drift.md`](../2026-08-19_schema_doc_drift.md)).

- 옵션 조회 경로는 `menu_opt_policy → opt_policy → opt_policy_item → opt_item`이다. `menu_option`은 레거시 명칭이다.
- 재료 제외와 선택 옵션은 `item_exclusion`, `order_item`, `order_item_option`에 저장한다.
- 결제수단과 상태 코드는 `pay_method_cfg`, `common_code`를 사용한다.
- 결제 승인은 `payment.idempotency_key`(`VARCHAR(64) NOT NULL UNIQUE`, DB 기본값 없음)로 멱등성을 보장한다. 클라이언트가 요청마다 UUID를 보내고 서버가 저장한다. 같은 키인데 `order_id`나 결제수단이 다르면 `IDEMPOTENCY_KEY_CONFLICT`(409)다. 수동 INSERT 시 이 컬럼을 빠뜨리면 `Field 'idempotency_key' doesn't have a default value`로 실패한다.
- 이미지는 `media_asset`에 모으고 `menu.image_asset_id`, `ing.icon_asset_id`, `ing.photo_asset_id`, `pay_method_cfg.image_asset_id`가 참조한다. 메뉴 조회는 `media_asset`을 `LEFT JOIN`한다. 배경은 [`MENU_IMAGE_ASSET_FLOW.md`](../MENU_IMAGE_ASSET_FLOW.md) 참고.
- `orders.order_no`와 `payment.order_id`는 UNIQUE다. 주문번호 중복 발급과 한 주문의 이중 결제 행을 DB가 막는다.
- 외래키 제약 이름은 옛 테이블 이름을 쓰는 것이 많다(`fk_ingredient_type`, `fk_payment_method_config_method` 등). `DROP FOREIGN KEY` 시 테이블명에서 유추하지 말고 실측본을 확인한다.
- 매출 API는 `vw_sales_daily`, `vw_sales_hourly`, `vw_top_menu_daily`, `vw_top_menu_hourly`를 읽기 원본으로 사용한다.
- 전액 환불의 순매출은 0이어야 하며, 취소/환불 주문은 인기 메뉴 집계에서 제외한다.
- `menu.active`는 실제 DB에 없으므로 새 필드로 가정하지 않는다.

## 구현 전·후 체크

1. DB에 실제 컬럼/뷰와 테스트 데이터가 있는지 확인한다.
2. request DTO에 Bean Validation과 오류 코드 규칙을 정한다.
3. Service에서 금액, 품절, 상태 전이, 트랜잭션 경계를 구현한다.
4. Mapper XML은 파라미터와 result mapping을 DTO 기준으로 명시한다.
5. 성공, 400 검증 오류, 404 없음, 409 충돌, 5xx 예외 응답을 Bruno로 확인한다.

## 정본 링크

- [테이블 DDL 실측본](../아삭_mysql.sql) · [뷰 정의](../view.sql) · [동기화 도구](../tools/README.md)
- [중앙 테이블 정의서](../../../ASAK/docs/wiki/db-table-definition.md) · [중앙 뷰 정의서](../../../ASAK/docs/wiki/db-view-definition.md)
- [API 계약 Bruno 안내](../../api/README.md)
- [메뉴 API 계약](../../../ASAK/docs/product_bible/03_Menu_Inventory_SoldOut/menu/MENU_API_CONTRACT.md)
- [주문 API 계약](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/order/ORDER_API_CONTRACT.md)
- [결제 API 계약](../../../ASAK/docs/product_bible/02_Order_Cart_Payment/payment/PAYMENT_API_CONTRACT.md)
- [정본 계약 결정](../../../ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md)
