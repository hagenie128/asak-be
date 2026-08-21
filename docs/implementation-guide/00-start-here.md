# ASAK 백엔드 구현 작업대

> 상태: Current
> 기준일: 2026-07-23
> 범위: `ASAK-back`의 Spring Boot API·MyBatis·DB 연동 작업. 화면을 새로 구현하는 문서가 아니다.

## 먼저 확인할 현재 상태

- `ApiResponse<T>`와 주문·결제 enum, 주문 생성 DTO 골격은 존재한다.
- Controller, Service, Mapper/XML에는 HTTP 매핑과 SQL이 아직 없다. 따라서 현재 Bruno 컬렉션은 `SPEC_ONLY`이며 2xx 응답을 기대하면 안 된다.
- 공통 예외 처리, 페이지 응답, 보안/JWT, Swagger, 정적 리소스 설정도 클래스 골격 단계다.
- 실제 진행 현황과 구현 우선순위는 [프로젝트 현황 보고](10-project-status-2026-07-23.md)를 먼저 확인한다.
- Admin/Kiosk의 mock·하드코딩 값과 백엔드 계약 차이는 [프론트 연동 감사](11-frontend-integration-audit-2026-07-23.md)에서 확인한다.
- Product Bible·Bruno·실제 소스에서 구현에 필요한 내용을 모은 [정본 참조 팩](12-canonical-reference-pack.md)을 다음 문서 탐색 전에 읽는다.

## 지금 할 API 선택

| 작업 | 먼저 볼 문서 | 다음 구현 단위 |
| --- | --- | --- |
| 공통 응답·예외·헬스체크 | [고정 규칙](01-fixed-rules.md) | `common` → health endpoint |
| 키오스크 메뉴 조회 | [Kiosk API 카드](02-kiosk-implementation.md) | Controller → Service → Mapper → XML → DTO |
| 장바구니 검증·주문·결제 | [Kiosk API 카드](02-kiosk-implementation.md) | 서버 금액 계산과 상태 전이 |
| 관리자 주문·품절·메뉴 | [Admin API 카드](03-admin-implementation.md) | 조회/변경 API를 세로로 완성 |
| 대시보드·매출 | [Admin API 카드](03-admin-implementation.md) | 매출 뷰 조회와 집계 DTO |
| DB 필드·응답 계약 | [API·DB 규칙](04-api-db-implementation.md) | 계약과 실제 스키마 대조 |
| 원본 문서 검색 없이 계약 확인 | [정본 참조 팩](12-canonical-reference-pack.md) | API 입력·출력, 상태 전이, DB·프론트 변환 기준 |
| 테스트·Bruno 검증 | [QA·릴리스](06-qa-release-guide.md) | 성공·오류·회귀 시나리오 |
| Admin/Kiosk mock 연동 | [프론트 연동 감사](11-frontend-integration-audit-2026-07-23.md) | 코드값·경로·mock 필드 어댑터 |

## 한 API를 끝내는 순서

1. Product Bible과 Bruno 요청에서 API ID, 경로, request/response를 확인한다.
2. 실제 DB 컬럼과 뷰를 대조하고 DTO 필드를 먼저 확정한다.
3. Controller → Service → Mapper interface → Mapper XML → response DTO 순서로 한 API를 완성한다.
4. 성공, 검증 실패, 품절/상태 충돌, 데이터 없음 응답을 Bruno와 테스트로 확인한다.
5. 구현 상태를 [기능 매트릭스](08-feature-implementation-matrix.md)와 현황 보고에 갱신한다.

## 절대 혼동하지 않을 것

- Product Bible/Bruno의 API는 목표 계약이고, 소스에 `@RequestMapping` 등이 생기기 전에는 실행 API가 아니다.
- 클라이언트가 보낸 금액·품절 여부·주문 상태를 정본으로 신뢰하지 않는다. Service가 DB 기준으로 검증하고 결정한다.
- Entity/Mapper 결과를 Controller에서 그대로 반환하지 않는다. 응답은 `ApiResponse<T>`와 기능별 DTO를 사용한다.
- 화면 문서와 코드가 다르면 임의로 계약을 바꾸지 말고 차이를 기록한다.

## 정본 링크

- [ASAK-back 구현 계획](../../IMPLEMENTATION_PLAN.md)
- [정본 계약 결정](../../../ASAK/docs/governance/canonical-contract-decisions-2026-07-16.md)
- [Product Bible 백엔드 구현 허브](../../../ASAK/docs/product_bible/11_Backend_Implementation/README.md)
- [정본 우선순위](../../../ASAK/docs/product_bible/01_Foundation/CANONICAL_SOURCE.md)
- [Bruno API 컬렉션](../../api/README.md)
