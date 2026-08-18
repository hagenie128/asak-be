# ASAK Bruno API Contract Collection

> Status: CONTRACT / PARTIALLY_IMPLEMENTED (2026-08-11)
> 경로·요청 본문 정본: `IMPLEMENTATION_PLAN.md` + Product Bible API Contract  
> DB 읽기 모델: `docs/view.sql` · 위키 `db-view-definition.md`

이 컬렉션은 백엔드 구현 전·중 API 계약을 Bruno에서 검토·호출하기 위한 요청 모음이다.

## 사용 방법

1. Bruno에서 이 `api` 폴더를 Collection으로 연다.
2. `Local` 환경을 선택하고 `baseUrl`을 실행 중인 백엔드 주소로 맞춘다.
3. Controller 매핑이 있는 요청:
   - 공통: `GET /api/health`
   - 키오스크: API-001~006, API-014 (카테고리·메뉴·장바구니 검증·주문·결제수단·결제 승인)
   - 관리자: API-007/008/011~013/021~024 (주문·메뉴) + 메뉴 삭제·카테고리·재료 보조 endpoint
   주문 생성은 헤더·품목·옵션·제외 재료 저장과 응답 조립까지 구현되어 있다.
   품절(API-009/010)·결제수단 설정(API-015/016)·매출(API-017~019)·대시보드(API-020)는 아직 비어 있다.
   현재 작업 트리의 옵션 그룹은 `GET /api/admin/opts/groups`, `GET /api/admin/opts/{optionGroupId}`가 Controller에 있다.

`Local` 환경에는 검토용 ID가 들어 있다. 실제 테스트 데이터의 메뉴·주문·결제수단 ID와
다르면 해당 환경 변수만 바꾼다. 결제수단 기본값 `methodId: 10828`은 `CARD`이며 화면 이름은
`카드·삼성페이`다.

## 요청 파일 형식

모든 HTTP 요청 `.bru`는 아래 순서를 따른다.

1. `meta` — `name`, `type: http`, `seq` (파일명 숫자와 동일)
2. method 블록 — `url`, `body`, `auth: none`
3. `headers` / `body:json` / `vars:pre-request` (필요 시)
4. `docs` — API 코드·경로·응답 code·주의사항
5. `tests` — **health만** 유지

파일명: `{seq}-{kebab-name}.bru`
JSON body는 camelCase만 사용한다.

## Admin 메뉴 순서 (05–12)

| seq | 파일 | API | 메서드 |
| --- | --- | --- | --- |
| 05 | menu-list | API-011 | GET |
| 06 | menu-detail | API-023 | GET |
| 07 | create-menu | API-012 | POST |
| 08 | update-menu | API-013 | PATCH |
| 09 | delete-menu | — (보조) | DELETE (자식 cascade) |
| 10 | menu-list-total | API-011 | GET |
| 11 | category-list | — (보조) | GET |
| 12 | ingredient-list | — (보조) | GET |

번호 정본: `../IMPLEMENTATION_PLAN.md` §4 · 루트 `../README.md` API 표.

## 주의

- 성공 응답 assert는 **health**에만 둔다. 나머지 API는 구현 완료 후 실제 계약 코드에 맞춰 추가한다.
- `KAKAO_PAY`, `NAVER_PAY`는 목록에 보이지만 `isEnabled: false`다. 결제 승인은 기본 `CARD`를 사용한다.
- 주문/장바구니 요청의 옵션은 `items[].optionItems[]`이며, 각 항목은
  `optionItemId`, `quantity`를 가진다. 재료 제외는 `excludedIngredientIds`다.
- 금액 응답은 `totalAmount`, 장바구니 항목 금액은 `unitAmount`를 사용한다.
  현재 주문 생성 DTO의 상태 필드는 `data.status`이며, 목록·상세의 상태 필드는
  `orderStatus`다. 외부 envelope의 HTTP 상태값 `status`와 혼동하지 않는다.
- wiki `rest-api-spec.md` 정본 path는 `/api/kiosk/**`, `/api/admin/**`이다. 구 `/api/menus` 표는 폐기.
- 메뉴 삭제 시 `ing` 마스터는 지우지 않는다. `order_item`이 있으면 `MENU_DELETE_FAILED`다.

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
`code`는 API별 문자열이다. (예: `HEALTH_OK`, `ADMIN_MENU_LIST_SUCCESS`)
