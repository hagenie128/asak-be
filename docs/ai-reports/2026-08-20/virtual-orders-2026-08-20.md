# 가상 주문 데이터 삽입 — 2026-08-20 / 2026-08-21

- 기록 시각: 2026-08-20 16:26 (Asia/Seoul), 2026-08-21분 추가 기록: 2026-08-20 16:40경
- 목적: 관리자 매출 대시보드 발표/시연용 가상 주문 데이터 생성
- 대상 DB: `asak_db` (운영 공용 DB, `nam3324.synology.me:33338`)
- 상태: 이틀치(8/20, 8/21) 삽입 완료

## 생성 조건 (사용자 확정)

- 하루 60건 이상 → 65건 생성
- 오늘(2026-08-20)만 우선 삽입, 내일(2026-08-21)은 나중에
- 취소/환불 5~10% 섞기 → 6건(9.2%) 취소

## 가격 계산 규칙

`UserOrderService.validateAndPriceItems()` 정본 규칙을 그대로 따름([[asak-order-data-integrity]] 참고, 기존 시드 데이터 73% 불일치를 반복하지 않기 위함):

```
order_item.price = menu.price + Σ(옵션 add_price × 옵션수량)
orders.total_price = Σ(order_item.price × order_item.quantity)
payment.amount = orders.total_price
order_item_option.price = opt_item.add_price
```

## 생성 방식

- 시간대: 10:00~16:20 (영업시간 시작 ~ 생성 시점 현재 시각까지만. 아직 안 지난 시간대는 넣지 않음)
  - 12:00~13:30 구간에 60% 가중치(점심 피크)
- 메뉴: 현재 `sold_out=0`인 활성 메뉴 70개 중 무작위 (이전에 품절 처리한 1167, 6811 등은 자동 제외됨)
- 주문당 아이템 1~3개(50/35/15%), 아이템당 수량 1~2개(85/15%)
- 각 아이템에 그 메뉴의 필수 옵션 정책(`opt_policy.required=1`, 드레싱/세트사이드/세트음료 등)의 default 항목을 자동으로 붙임 (품절 항목은 자동 제외)
- 25% 확률로 토핑 추가 1개(`TOPPING` 그룹, 가격 반영)
- 주문유형: TAKE_OUT 60% / EAT_IN 40%
- 결제수단: 카드 50% / 카카오페이 25% / 네이버페이 15% / 토스페이 10%
- 취소 주문: 결제 승인(`paid_at`) 후 결제 상태를 `REFUNDED`, 주문 상태를 `CANCELED`로 — gross/canceled 매출 양쪽에 잡히도록 함(뷰 계산 규칙과 일치)
- `payment.idempotency_key` 는 기존 규칙대로 `demo-{order_id}-{hex12}`

## 결과

- 삽입된 `orders.id` 범위: **51753 ~ 51817** (65건)
- 취소/환불: 6건 (9.2%)
- 총 결제금액(gross): 997,700원 / 순매출(net): 883,400원
- `vw_sales_daily` 재조회 결과 (2026-08-20):

  | order_count | canceled_order_count | gross | canceled | net | avg_order | cancel_rate |
  |---|---|---|---|---|---|---|
  | 59 | 6 | 997,700 | 114,300 | 883,400 | 14,973 | 9.23% |

## 되돌리기(전체 삭제) SQL

필요시 이 배치로 만든 데이터만 정확히 지울 수 있다 (id 범위가 이 배치와 정확히 일치, 다른 실주문과 안 섞임):

```sql
DELETE FROM payment WHERE order_id BETWEEN 51753 AND 51817;
DELETE FROM order_item_option WHERE order_item_id IN (SELECT id FROM order_item WHERE order_id BETWEEN 51753 AND 51817);
DELETE FROM order_item WHERE order_id BETWEEN 51753 AND 51817;
DELETE FROM orders WHERE id BETWEEN 51753 AND 51817;
```

## 2026-08-21분 추가 삽입

같은 조건·같은 가격 계산 규칙으로 진행. 8/20과 차이점만 적음.

- 8/20은 "생성 시점 현재 시각까지만" 넣었지만, 8/21은 이미 미래 날짜 전체이므로 **영업시간 풀타임(10:00~22:00)** 으로 생성
- 68건 생성 (하루 60건 이상 기준 유지), 랜덤 시드만 다르게 사용(20260821)

### 결과 (8/21)

- 삽입된 `orders.id` 범위: **51818 ~ 51885** (68건)
- 취소/환불: 7건 (10.3%)
- 총 결제금액(gross): 1,060,400원 / 순매출(net): 903,200원
- `vw_sales_daily` 재조회 결과 (2026-08-21):

  | order_count | canceled_order_count | gross | canceled | net | avg_order | cancel_rate |
  |---|---|---|---|---|---|---|
  | 61 | 7 | 1,060,400 | 157,200 | 903,200 | 14,807 | 10.29% |

### 되돌리기(전체 삭제) SQL — 8/21분만

```sql
DELETE FROM payment WHERE order_id BETWEEN 51818 AND 51885;
DELETE FROM order_item_option WHERE order_item_id IN (SELECT id FROM order_item WHERE order_id BETWEEN 51818 AND 51885);
DELETE FROM order_item WHERE order_id BETWEEN 51818 AND 51885;
DELETE FROM orders WHERE id BETWEEN 51818 AND 51885;
```

## 남은 작업 / 미검증

- item_exclusion(재료 빼기)은 두 배치 모두 생성하지 않음 — 데모 매출 숫자와는 무관해서 생략
- 인기 메뉴 뷰(`vw_top_menu_daily`, `vw_top_menu_hourly`)나 관리자 화면에서 실제로 어떻게 보이는지는 브라우저로 확인하지 않음(DB 값만 확인)
- 이 DB는 팀 공용 운영 DB이므로, 다른 팀원이 같은 시간대에 실주문을 넣었을 가능성은 낮지만 완전히 배제되지는 않음
- 8/21 주문은 시스템 시계 기준 미래 날짜라, "미래 날짜 제외" 로직이 있는 화면/API에서는 아직 안 보일 수 있음(예: 30분 매출 API 설계 문서의 "미래 날짜는 반환하지 않는다" 규칙) — 해당 로직이 실제로 어떻게 걸리는지는 확인 안 함
