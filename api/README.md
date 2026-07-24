# ASAK Bruno API Contract Collection

> Status: CONTRACT (2026-07-24)  
> 경로·요청 본문 정본: `IMPLEMENTATION_PLAN.md` + Product Bible API Contract  
> DB 읽기 모델: `docs/view.sql` · 위키 `db-view-definition.md`

이 컬렉션은 백엔드 구현 전·중 API 계약을 Bruno에서 검토·호출하기 위한 요청 모음이다.

## 사용 방법

1. Bruno에서 이 `api` 폴더를 Collection으로 연다.
2. `Local` 환경을 선택하고 `baseUrl`을 실행 중인 백엔드 주소로 맞춘다.
3. 현재 Controller 매핑이 있는 요청만 2xx를 기대한다. (지금: `GET /api/health`)

`Local` 환경에는 검토용 ID가 들어 있다. 실제 테스트 데이터의 메뉴·주문·결제수단 ID와
다르면 해당 환경 변수만 바꾼다. 결제수단 기본값 `methodId: 10828`은 `CARD`이며 화면 이름은
`카드·삼성페이`다.

## 주의

- 키오스크·관리자 Controller HTTP mapping은 아직 대부분 비어 있다. 해당 요청은 계약 확인용이다.
- 성공 응답 assert는 **health**에만 둔다. 나머지 API는 구현 후 계약 코드(`MENU_LIST_SUCCESS` 등)에 맞춰 추가한다.
- `KAKAO_PAY`, `NAVER_PAY`는 목록에 보이지만 `isEnabled: false`다. 결제 승인은 기본 `CARD`를 사용한다.
- 주문/장바구니 요청 필드는 `selectedOptionItemIds`, `excludedIngredientIds`다. (`optionItems`는 응답 쪽 필드)
- wiki `rest-api-spec.md`의 `/api/menus`, `/api/orders` 등은 **레거시**다. Bruno는 `/api/kiosk/**`, `/api/admin/**`만 쓴다.

## API ↔ DB 뷰 매핑 (구현 시)

| API | 주요 뷰 / 읽기 모델 |
| --- | --- |
| 메뉴 목록 | `vw_menu_list` (+ `vw_menu_availability`) |
| 메뉴 상세 | `vw_menu_ing_json`, `vw_menu_opt_policy_json` (+ [15] allergens 헤더) |
| 결제 승인 | `vw_payment_result` |
| 결제수단 목록 | 인라인 (`view.sql` [17] · `pay_method_cfg`) |
| 품절 관리 | `vw_soldout_catalog` · 영향 메뉴 수는 인라인 ([18]) |
| 주문 목록 | `vw_order_list_summary` |
| 주문 상세 | `vw_order_summary`, `vw_order_item_full` |
| Live 보드 | `vw_order_live`, `vw_order_item_base_dressing`, `vw_order_item_tag` |
| 대시보드 | `vw_order_status_summary`, `vw_sales_daily`, `vw_top_menu_daily` |
| 매출 | `vw_sales_daily`, `vw_sales_hourly`, `vw_top_menu_*` |

## 기준 문서

- `ASAK-back/IMPLEMENTATION_PLAN.md`
- `ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md`
- `ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md`
- `ASAK/docs/product_bible/**/*API*.md`
- `ASAK/docs/wiki/db-view-definition.md`
- `ASAK-back/docs/view.sql`

모든 응답 계약은 `{ success, status, code, message, data }` 형식을 따른다.  
`code`는 API별 문자열이다. (예: `HEALTH_OK`, `MENU_LIST_SUCCESS`)
