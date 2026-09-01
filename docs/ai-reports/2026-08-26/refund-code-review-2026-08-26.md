# 관리자 환불 구조 코드 리뷰 (2026-08-26)

## 범위와 검증 상태

- 대상: `PATCH /api/admin/orders/{orderId}/refund`의 DTO, Controller, Service, Mapper/XML
- 코드 변경: 없음. 이 문서는 현재 작업 트리의 리뷰 기록이다.
- 실행 검증: `gradlew.bat compileJava` **성공**
- 미검증: Spring context 기동, MyBatis statement 로딩, 실제 DB 조회/갱신, HTTP/Bruno, 실제 PG 취소

## 반영된 구조

| 항목 | 확인 결과 | 근거 |
|---|---|---|
| API 요청 DTO 분리 | 반영됨. 클라이언트 body는 `refundReason`만 가진다. | `OrderRefundRequest` |
| 내부 조회 DTO | 반영됨. `RefundTarget`에 주문·결제 상태, 결제수단, 금액, provider 결제 키 필드를 둔다. | `RefundTarget`, `findRefundTarget` |
| 카드만 MVP 지원 | 반영됨. `CARD`만 가상 취소키를 생성하고 나머지는 오류로 차단한다. | `AdminOrderService.refundOrder`, `PaymentService.cardRefund` |
| static 제거 | 반영됨. `PaymentService`는 Spring Bean의 인스턴스 메서드다. | `PaymentService` |
| 일반 주문취소와 환불 주문 상태 변경 분리 | 반영됨. `cancelOrderForRefund`를 별도 Mapper로 호출한다. | `AdminPaymentMapper` |

## 수정 전 해결해야 할 문제

### 1. `payment_refund` INSERT 컬럼명이 실제 적용 SQL과 다르다 — 높음

실제 적용한 migration의 컬럼은 `reason`, `provider_cancel_transaction_key`다. 그러나 현재 실행 경로인 `AdminPaymentMapper.xml`은 `refund_reason`, `cancel_transaction_key`를 사용한다.

```text
실제 DB: reason, provider_cancel_transaction_key
현재 Mapper: refund_reason, cancel_transaction_key
```

이 상태에서는 `insertPaymentRefund()` 실행 시 unknown column SQL 오류가 발생한다.

### 2. `provider_payment_key` 조회 컬럼이 아직 DB에 없다 — 높음

`findRefundTarget`은 `p.provider_payment_key AS providerPaymentKey`를 조회한다. 하지만 실행 완료된 migration은 `provider`를 잠시 추가한 뒤 마지막 SQL에서 제거했고, `provider_payment_key`는 추가하지 않았다.

따라서 환불 대상 조회 자체가 실패한다. 가상 카드 MVP에 provider 키가 필요 없으면 SELECT/DTO에서 일단 제외하고, 실제 토스 연동을 시작할 때 `provider`와 `provider_payment_key`를 별도 migration으로 함께 도입해야 한다.

### 3. DB 반영이 하나의 트랜잭션이 아니다 — 높음

현재 순서는 아래와 같다.

```text
PaymentService.cardRefund()  // 외부 PG 호출을 대신하는 가상 취소키 생성
→ payment 상태 UPDATE
→ payment_refund INSERT
→ orders 상태 UPDATE
```

`AdminOrderService` 및 `applyRefund()`에 동작하는 `@Transactional` 경계가 없다. 예를 들어 refund INSERT가 실패하면 payment만 `REFUNDED`가 될 수 있다.

외부 PG 호출은 트랜잭션 밖에서 완료하고, 성공 후의 payment UPDATE + refund INSERT + orders UPDATE를 **별도 Spring Bean의 public `@Transactional` 메서드**로 묶어야 한다. 같은 클래스의 private 메서드에 `@Transactional`만 붙이면 Spring proxy를 거치지 않아 적용되지 않는다.

### 4. payment UPDATE의 동시성 조건이 빠졌다 — 높음

Service가 조회 시점에 `APPROVED`를 확인해도, UPDATE 전 다른 요청이 상태를 변경할 수 있다. `AdminPaymentMapper.xml`의 `markPaymentRefunded`는 `id = #{paymentId}`만 조건으로 사용한다.

`AND status_id = #{approvedStatusId}` 조건을 추가하고, 영향 행이 0이면 중복/경합 환불 오류로 처리해야 한다.

### 5. 환불용 주문 취소 SQL이 취소 시각과 상태 조건을 누락했다 — 높음

`AdminPaymentMapper.xml`의 `cancelOrderForRefund`는 `status_id`만 바꾼다.

- `canceled_at = NOW()`가 없어 주문 취소 시각이 기록되지 않는다.
- `AND status_id != #{canceledStatusId}` 같은 조건이 없어 동시 요청의 상태 변화를 SQL에서 방어하지 못한다.

### 6. 환불 Mapper가 두 군데에 중복되어 있다 — 중간

`AdminOrderMapper.xml`에도 `insertPaymentRefund`, `markPaymentRefunded`, `findRefundTarget`이 남아 있고, Service는 `AdminPaymentMapper`의 INSERT/UPDATE를 호출한다. 두 XML의 컬럼명도 서로 다르다.

환불 DB 반영 책임을 `AdminPaymentMapper` 또는 `AdminOrderMapper` 한 곳으로 정한 뒤, 다른 쪽의 사용하지 않는 statement와 인터페이스 메서드(`refundOrder`)를 정리해야 한다.

### 7. 여러 결제 시도 중 환불 대상을 고르는 규칙이 불명확하다 — 중간

`findRefundTarget`은 `ORDER BY p.id DESC LIMIT 1`이다. 이제 주문당 여러 payment 행이 가능하므로, 가장 최근 payment가 실패/대기 상태라면 이전의 승인 결제를 환불하지 못한다.

전액 환불 MVP라면 “가장 최근 `APPROVED` payment”를 선택할지, 또는 API에서 `paymentId`를 받되 서버가 주문 소유/승인 상태를 검증할지 정책을 확정해야 한다.

### 8. `refundReason`의 필수 검증이 없다 — 중간

`OrderRefundRequest.refundReason`에 `@NotBlank`가 없고 Controller에도 `@Valid`가 없다. 실제 토스 취소 API의 `cancelReason`은 필수이므로, PG 연동 전 요청 검증과 길이 제한을 정해야 한다.

## 현재 결론

구조 개편 방향은 맞고 Java 컴파일은 통과한다. 다만 현재 상태는 **환불 API를 실제 실행 가능한 완료 상태로 볼 수 없다.** 먼저 아래 순서로 팀원이 수정한 뒤 MyBatis/DB/HTTP를 검증해야 한다.

1. 실제 migration 컬럼명에 맞춰 환불 Mapper를 하나로 통일한다.
2. 존재하지 않는 `provider_payment_key` 조회를 제거하거나 별도 migration을 확정한다.
3. 외부 취소 성공 이후 DB 반영 전용 public `@Transactional` 메서드를 만든다.
4. payment/order UPDATE에 상태 조건과 `canceled_at`을 추가한다.
5. 여러 payment 중 환불 대상 선택 규칙과 `refundReason` 검증을 확정한다.

## 후속 수정 확인 (2026-08-26)

아래 항목은 리뷰 뒤 현재 코드에 반영된 것을 확인했다.

- `findRefundTarget`은 `APPROVED` 조건과 `paid_at DESC, id DESC` 정렬을 사용한다.
- 주문 존재 확인과 승인 결제 없음 오류를 구분한다.
- payment UPDATE 조건에 기존 `APPROVED` 상태를 포함한다.
- 환불 이력 INSERT 컬럼은 실제 migration의 `reason`, `provider_cancel_transaction_key`와 일치한다.
- `AdminRefundTransactionService`의 public `@Transactional` 메서드가 payment UPDATE, refund INSERT, order UPDATE를 하나로 묶는다.
- 주문 환불 UPDATE는 `canceled_at = NOW()`와 이미 취소된 상태 제외 조건을 사용한다.
- Controller는 `@Valid`를 사용하고, `refundReason`은 공백 불가 및 최대 200자 검증을 사용한다.

이번 수정으로 `RefundTarget` MyBatis result type을 실제 패키지(`com.asak.admin.dto.RefundTarget`)로 정정했고, 실제 DB에 아직 없는 `payment.provider_payment_key` 조회를 제거했다. 실제 PG 연동을 시작할 때 provider와 원 결제 키 컬럼을 별도 migration으로 함께 도입해야 한다.

## 관련 파일

- `src/main/java/com/asak/admin/dto/RefundTarget.java`
- `src/main/java/com/asak/admin/dto/request/orders/OrderRefundRequest.java`
- `src/main/java/com/asak/admin/service/AdminOrderService.java`
- `src/main/java/com/asak/admin/service/PaymentService.java`
- `src/main/java/com/asak/admin/mapper/AdminOrderMapper.java`
- `src/main/java/com/asak/admin/mapper/AdminPaymentMapper.java`
- `src/main/resources/mappers/AdminOrderMapper.xml`
- `src/main/resources/mappers/AdminPaymentMapper.xml`
