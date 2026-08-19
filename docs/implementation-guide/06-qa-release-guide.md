# 백엔드 QA·릴리스 가이드

## API 하나의 테스트 순서

1. Service 단위 테스트: 가격, 품절, 필수 옵션, 상태 전이 같은 비즈니스 규칙을 검증한다.
2. Mapper 통합 테스트: 실제 스키마/뷰, join, null, 빈 목록, 집계값을 확인한다.
3. Controller 계약 테스트: 경로·method·request validation·response envelope·HTTP status를 확인한다.
4. Bruno smoke: 프론트가 사용할 정상 요청과 대표 오류 요청을 실행한다.
5. 회귀: 주문/결제 변경 뒤 Admin 주문·매출 집계가 깨지지 않는지 확인한다.

## 공통 응답 체크

| 상황 | 기대 HTTP status | 기대 code 예시 |
| --- | --- | --- |
| 정상 조회 | 200 | `0000` |
| 정상 생성 | 201 | `0000` 또는 생성용 업무 코드 |
| request 형식/필수값 오류 | 400 | `1001` 등 validation code |
| 대상 없음 | 404 | `2001`(메뉴), `3002`(주문) |
| 품절·가격·상태 충돌 | 409 | `2002`, `3001`, `3003` |
| 예상 밖 오류 | 500 | `9000`으로 확장 예정 |

모든 경우 `{ success, status, code, message, data }` 형식을 유지하는지 확인한다. field 오류는 `data.field`, 재시도 가능 여부는 `data.canRetry`처럼 프론트가 사용할 정보를 합의된 형식으로 반환한다.

## P0 시나리오

- 품절 메뉴/옵션을 장바구니에 담은 뒤 주문 직전에 409로 막힌다.
- 클라이언트 가격을 변조해도 서버 `totalAmount`가 DB 가격으로 계산된다.
- 같은 결제 요청이 반복되어도 이중 승인되지 않는다.
- 허용되지 않은 주문 상태 전이와 완료/기취소 주문 취소가 409다.
- 승인 결제 취소는 주문·결제 상태와 매출 순매출에 함께 반영된다.
- 결제수단을 비활성화하면 Kiosk 결제수단 조회/승인이 일관되게 막힌다.

## 릴리스 전 체크

- [ ] `.env`와 DB 비밀값은 저장소/응답 로그에 포함하지 않았다.
- [ ] DB migration/seed 또는 실제 테스트 데이터로 재현했다.
- [ ] Bruno 요청이 Controller 구현 상태와 일치한다. 미구현 요청은 `SPEC_ONLY`로 남겼다.
- [ ] 테스트 결과와 남은 제한 사항을 현황 보고에 기록했다.
- [ ] API 문서의 경로/필드/상태 코드가 구현과 다르면 차이를 먼저 보고했다.

## 정본 링크

- [API smoke checklist](../../../ASAK/docs/product_bible/11_Backend_Implementation/08-testing/API_SMOKE_CHECKLIST.md)
- [백엔드 테스트 계획](../../../ASAK/docs/product_bible/11_Backend_Implementation/08-testing/BACKEND_TEST_PLAN.md)
- [API 계약 테스트](../../../ASAK/docs/product_bible/09_QA_Bible/03-api-backend/API_CONTRACT_TESTS.md)
