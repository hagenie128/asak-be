# 백엔드 구조·DTO 가이드

> 이 문서는 기존 UI 컴포넌트 가이드를 백엔드 구현 경계 기준으로 바꾼 것이다.

## 패키지 경계

현재 `com.asak.admin`, `com.asak.user`, `com.asak.common` 구조를 유지한다. 기능 구현 중 전체 패키지를 새 도메인 구조로 이동하지 않는다.

| 위치 | 역할 | 현재 주의점 |
| --- | --- | --- |
| `controller` | HTTP mapping, request validation, response 반환 | 현재 빈 클래스라 API별 `@RestController`/mapping 필요 |
| `service` | 비즈니스 규칙, 상태 전이, 트랜잭션 | 가격·품절·권한 로직을 Controller에 넣지 않음 |
| `mapper` + `resources/mappers` | MyBatis interface/SQL/result mapping | interface와 XML namespace/메서드명을 일치 |
| `dto` | request/response 계약 | 빈 `request.java`, `response.java` 대신 기능별 명확한 클래스 사용 |
| `common` | response, exception, enum, config | 공통 기반은 첫 API 전 실제 구현 필요 |

## DTO 작성 순서

1. Bruno request와 Product Bible API 계약에서 입력/출력 필드를 표로 뽑는다.
2. request DTO에는 Bean Validation, response DTO에는 화면에 필요한 계산 완료 값만 둔다.
3. Mapper 결과가 곧바로 API가 되지 않도록 조합이 필요한 값은 Service에서 response DTO로 만든다.
4. `ApiResponse<ResponseDto>`로 감싸고 오류는 예외 handler가 같은 envelope로 반환한다.

## 기존 골격 사용 시 주의

- `CreateOrderRequest`와 주문 enum은 계약 출발점이지만 아직 validation/service 연결이 없다.
- `PageResult`, 예외/보안/config 클래스는 이름만 존재한다. import가 된다고 구현 완료가 아니다.
- MyBatis 설정이 있어도 빈 XML은 SQL이 실행되지 않는다. 하나의 조회 API로 mapper scan과 result mapping을 먼저 검증한다.

## 정본 링크

- [DTO·Mapper 규칙](../../../ASAK/docs/product_bible/06_Engineering_Bible/03-backend/DTO_AND_MAPPER_RULES.md)
- [서비스·트랜잭션 규칙](../../../ASAK/docs/product_bible/06_Engineering_Bible/03-backend/SERVICE_TRANSACTION_RULES.md)
