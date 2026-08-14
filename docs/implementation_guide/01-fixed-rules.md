# ASAK 백엔드 고정 규칙

> 상태: Current · 기준일: 2026-07-23

## 정본과 저장소 책임

| 구분 | 기준 |
| --- | --- |
| 구현 저장소 | `ASAK-back` — Spring Boot 4.0.7, Java 25, Gradle |
| 제품 정책·화면·API/DB 계약 | `ASAK/docs/product_bible` 및 최신 합의 문서 |
| 실제 데이터 구조 | 연결 DB의 스키마·매출 뷰. 계약과 다르면 차이를 기록하고 임의 변경하지 않음 |
| 실행 가능한 API | Controller에 매핑이 있고 테스트/Bruno로 확인된 것만 표기 |

## 계층 책임

```text
Controller → Service → Mapper interface → Mapper XML → Database
                 ↓
            Request/Response DTO
```

- Controller: HTTP 요청 수신, DTO 검증, Service 호출, HTTP 응답 반환
- Service: 가격 재계산, 품절/필수 옵션 검사, 상태 전이, 트랜잭션
- Mapper/XML: 조회·조인·필터·집계 SQL
- DTO: API 계약 전용 데이터. Entity 또는 Mapper 결과를 그대로 외부에 노출하지 않음

## 응답·명명 규칙

`ASAK-back`의 신규 API는 2026-07-23 API 정렬 문서와 `IMPLEMENTATION_PLAN.md`에 따라 아래 5개 공통 필드를 사용한다.

```json
{"success": true, "status": 200, "code": "0000", "message": "요청이 성공했습니다.", "data": {}}
```

- Java/JSON은 camelCase, DB 컬럼은 기존 snake_case를 Mapper에서 연결한다.
- `code`는 숫자처럼 보이는 **문자열 업무 코드**다. 성공은 `"0000"`, 옵션은 `"1001"`, 메뉴는 `"2001"~`, 주문은 `"3001"~`, 결제는 `"4001"~`를 사용한다. HTTP `status`와 같은 숫자를 재사용하지 않는다.
- Java의 enum 이름(`ORDER_NOT_FOUND`)은 개발용 식별자이고, API 응답 `code`는 대응하는 업무 코드(`"3002"`)다.
- Product Bible의 [API Design Rules](../../../ASAK/docs/product_bible/06_Engineering_Bible/05-api/API_DESIGN_RULES.md)는 아직 `success/message/data` 3개 필드 예시를 갖고 있다. 이 저장소에서는 더 최신인 [DevCopilot API 정리 기준](../../../ASAK/docs/governance/devcopilot-api-alignment-2026-07-23.md)의 5개 필드를 적용하며, Product Bible 원문 정리는 별도 문서 작업으로 남긴다.
- `total_price → totalAmount`, `paid_at → approvedAt`, `sold_out → isSoldOut`을 사용한다.
- `category` 테이블에는 코드 컬럼이 없으므로 `categoryCode`를 만들지 않고 `categoryId`를 사용한다.
- 페이지 목록은 `PageResult` 구현을 먼저 확정한 뒤 모든 목록 API에 같은 형태로 적용한다.

## 주문·결제 불변 규칙

- 주문 유형: `EAT_IN`, `TAKE_OUT`
- 주문 상태: `RECEIVED → PREPARING → COMPLETED`, 취소는 `CANCELED`
- 결제 상태: `READY`, `APPROVED`, `FAILED`, `CANCELED`, `REFUNDED`
- 결제 수단: `CARD`, `KAKAO_PAY`, `NAVER_PAY`; 삼성페이는 `CARD`에 포함
- 서버가 메뉴/옵션 가격, 품절, 수량, 필수 옵션을 다시 검증하여 `totalAmount`를 계산한다.
- 승인 결제를 취소하면 원 `paid_at`은 보존하고 결제 상태와 `refunded_at`을 갱신한다. 완료/기취소 주문 취소는 `409 ORDER_CANCEL_NOT_ALLOWED`다.

## 현재 미완성 공통 기반

`GlobalExceptionHandler`, `ErrorCode`, `PageResult`, Security/JWT, Swagger, WebMvc 설정은 현재 골격이다. 관련 API를 구현할 때 이 항목을 함께 실제 동작으로 완성하고, 골격만 존재한다는 이유로 완료 처리하지 않는다.

## 정본 링크

- [ASAK-back 구현 계획](../../IMPLEMENTATION_PLAN.md)
- [백엔드 아키텍처](../../../ASAK/docs/product_bible/06_Engineering_Bible/03-backend/BACKEND_ARCHITECTURE.md)
- [공통 응답 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/01-common/COMMON_RESPONSE_IMPLEMENTATION.md)
- [예외 구현 기준](../../../ASAK/docs/product_bible/11_Backend_Implementation/01-common/EXCEPTION_IMPLEMENTATION.md)
- [검증·예외 규칙](../../../ASAK/docs/product_bible/06_Engineering_Bible/03-backend/VALIDATION_AND_EXCEPTION_RULES.md)
