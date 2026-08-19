# ASAK-back 프로젝트 현황 보고

> 상태: Current · 확인일: 2026-07-23
> 확인 범위: `ASAK-back` 소스, `IMPLEMENTATION_PLAN.md`, Bruno API 컬렉션, Product Bible·governance 문서

## 요약

백엔드는 Spring Boot 프로젝트와 도메인별 패키지 구조, 공통 응답 필드, 주문 생성 DTO·enum, Bruno 목표 API 계약까지 준비되어 있다. 다만 HTTP Controller 매핑, Service 비즈니스 로직, Mapper SQL/XML, 공통 예외 처리, 테스트는 아직 구현 전이다. 따라서 현재는 **API 계약·스캐폴드 정렬 단계**이며, 기능 구현 완료 단계가 아니다.

## 확인된 구현 상태

| 영역 | 확인된 파일/자산 | 상태 |
| --- | --- | --- |
| 실행 기반 | Spring Boot 4.0.7, Java 25, Gradle, MySQL·MyBatis 의존성 | 준비됨 |
| 공통 응답 | `common/response/ApiResponse.java`에 `success/status/code/message/data` 필드 | 골격만 존재 |
| 주문 계약 | `CreateOrderRequest`, `OrderItemRequest`, `OptionItemRequest`, 주문/결제 enum | DTO·enum 준비됨 |
| 사용자·관리자 구조 | `user`, `admin`, `common`의 Controller/Service/Mapper/XML 경로 | 빈 클래스·빈 Mapper 단계 |
| 예외·보안·문서화 | ErrorCode, GlobalExceptionHandler, JWT, Swagger, WebMvc | 골격만 존재 |
| API 계약 | `api/` Bruno 요청 24개 | `SPEC_ONLY` |
| 테스트 | Spring Boot context test 1개 | API 계약·비즈니스 테스트 미작성 |

## 현재 정본 API

| 우선순위 | 범위 | 대표 경로 |
| --- | --- | --- |
| 1 | Kiosk 메뉴 조회 | `GET /api/kiosk/categories`, `menuList`, `menuDetail/{menuId}` |
| 2 | 장바구니·주문·결제 | `POST /api/kiosk/cart/validate`, `orders`, `payments` |
| 3 | Admin 주문 | `GET /api/admin/orders/active`, `orders`, `orders/{orderId}`, 상태 변경·취소 |
| 4 | Admin 메뉴·품절 | `GET/POST/PATCH /api/admin/menus`, `PATCH/GET /api/admin/soldOut` |
| 5 | 운영·매출 | 결제수단, dashboard, sales daily/summary/monthly |

목록의 모든 경로는 계약이며, 현재 Controller에는 HTTP mapping annotation이 없으므로 실행 확인된 endpoint는 아니다.

## 공통 기반 선행 작업

1. `ApiResponse` 성공/실패 생성 방식과 `PageResult` 목록 형식을 정한다.
2. `ErrorCode`에 HTTP 상태·코드·메시지 정책을 넣고 `BusinessException`을 연결한다.
3. `GlobalExceptionHandler`에서 Bean Validation(400), 미존재(404), 상태/품절/가격 충돌(409), 예상 밖 예외(500)를 공통 envelope로 반환한다.
4. MyBatis mapper scan과 XML SQL/result mapping을 실제 첫 API로 검증한다.
5. 최소 `GET /api/health`를 추가한 뒤 메뉴 조회 API부터 세로로 구현한다.

## 계약 정렬 메모

- 이 저장소의 적용 응답은 `{ success, status, code, message, data }`다. 이는 2026-07-23 DevCopilot API 정리와 `IMPLEMENTATION_PLAN.md`에 명시되어 있다.
- Product Bible의 `API_DESIGN_RULES.md`에는 3필드 예시가 남아 있어 문서 간 차이가 있다. 이번 백엔드 가이드는 2026-07-23 정렬 기준을 따르며, Product Bible 원문 갱신은 별도 정리 대상이다.
- `categoryCode`는 쓰지 않고 `categoryId`를 사용한다. 메뉴 활성화용 `menu.active`는 실제 DB에 없으므로 가정하지 않는다.
- 메뉴 옵션은 `menu_opt_policy → opt_policy → opt_policy_item → opt_item` 경로, 매출은 `vw_sales_*` 뷰를 기준으로 구현한다.
- Admin/Kiosk에는 mock 단계의 상태 코드·경로·표시 필드가 남아 있다. 백엔드 API를 연결하기 전 필요한 변환은 [프론트 연동 감사](11-frontend-integration-audit-2026-07-23.md)에 기록했다.

## 다음 작업 권장 순서

공통 응답·예외 → health → 카테고리/메뉴 목록/상세 → 장바구니 검증/주문 → 결제 → 관리자 주문 → 품절/메뉴 → 대시보드·매출 순서다. 각 단계는 Controller부터 DB와 Bruno 확인까지 하나의 세로 기능으로 끝낸다.

## 근거 문서

- [ASAK-back 구현 계획](../../IMPLEMENTATION_PLAN.md)
- [Bruno 컬렉션 상태](../../api/README.md)
- [DevCopilot API 정리 기준](../../../ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md)
- [백엔드 로드맵](../../../ASAK/docs/product_bible/11_Backend_Implementation/00-plan/BACKEND_IMPLEMENTATION_ROADMAP.md)
- [예외 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/01-common/EXCEPTION_IMPLEMENTATION.md)
