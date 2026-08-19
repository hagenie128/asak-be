# 주문 금액 정합성 조사

> 작성일: 2026-08-19 (수치 기준 17:14)
> 대상: `orders`, `order_item`, `order_item_option`, `payment`
> 상태: **조사만. 수정하지 않았다.** 권고는 7절.

## 1. 결론

세 줄로 요약된다.

1. **코드는 정상이다.** 2026-08 의 실제 주문은 규칙을 100% 지킨다.
2. **고객이 낸 금액도 정확하다.** `payment.amount` 와 `orders.total_price` 가 51,731건 전부 일치하고,
   `total_price` 에는 옵션 추가금이 들어 있다.
3. **깨진 것은 시드 데이터의 `order_item.price` 하나다.** 아이템 단가에 옵션 추가금이 빠져 있다.

즉 **청구 오류가 아니라 과거 시드 데이터의 기록 누락**이다. 급히 고칠 일이 아니며, 오히려 고치려다
과거 주문 기록을 훼손할 위험이 있다(6절).

## 2. 정본 규칙

`UserOrderService.validateAndPriceItems()` 가 정본이다.

```java
int unitPrice = menu.getPrice();
// 선택한 옵션마다
unitPrice += optionInfo.getExtraPrice() * option.getQuantity();
int lineAmount = unitPrice * item.getQuantity();
totalAmount += lineAmount;
```

정리하면 이렇다.

| 값 | 규칙 |
|---|---|
| `order_item.price` | `menu.price + Σ(옵션 add_price × 옵션수량)` — **옵션이 포함된 단가** |
| `orders.total_price` | `Σ(order_item.price × order_item.quantity)` |
| `payment.amount` | `orders.total_price` |
| `order_item_option.price` | `opt_item.add_price` — 옵션 단가 |

`order_item.price` 가 "메뉴 가격"이 아니라 **"옵션까지 포함한 아이템 단가"** 라는 점이 핵심이다.
이름만 보고 메뉴 가격으로 오해하기 쉽다.

## 3. 실태

### 3-1. `order_item.price`

| 구분 | 건수 | 규칙 일치 |
|---|---|---|
| 전체 | 83,190 | 33,932 (40.8%) |
| 옵션 없는 아이템 | 32,762 | 32,762 (100%) |
| 옵션 있는 아이템 | 50,428 | **1,170 (2.3%)** |

옵션이 있는 아이템 중 **49,258개가 `menu.price` 만 담고 있다.** 옵션 추가금이 통째로 빠졌다.

40.8% 라는 전체 수치는 우연이 아니다. 옵션 없는 아이템 비율(32,762 / 83,190 = **39.4%**)과 거의
같다. 즉 **시드는 옵션 추가금을 아예 반영하지 않았고, 옵션이 없어서 우연히 규칙과 맞은 것들만
일치로 잡힌 것**이다.

### 3-2. 시기별 — 여기서 원인이 갈린다

| 기간 | 아이템 | 규칙 일치 |
|---|---|---|
| 2026-08 (실제 주문) | 1,962 | **1,962 (100%)** |
| 2026-07 | 4,494 | 1,742 (38.8%) |
| 2026-06 | 6,016 | 2,379 (39.5%) |
| 2026-05 | 6,072 | 2,400 (39.5%) |
| 2025-06 ~ 2026-04 | — | 37~41% 로 일정 |

**2026-08 만 100% 다.** 이 구간이 실제로 애플리케이션을 거쳐 만들어진 주문이고, 그 이전은 전부
시드다. 코드가 정상이라는 근거가 여기 있다.

> 2026-08 은 한때 98.1% 였다. 중복 옵션 통합 과정에서 37건이 깨졌고 같은 날 보정해 100% 로
> 되돌렸다. 경위는 `duplicate-option-cleanup-plan-2026-08-19.md` 0-2절.

### 3-3. `orders.total_price` — 이쪽은 대체로 맞다

| 확인 | 결과 |
|---|---|
| `Σ(order_item.price × quantity)` 와 일치 | 14,846 / 51,750 (28.7%) |
| **`Σ((menu.price + 옵션가) × quantity)` 와 일치** | **45,164 / 51,750 (87.3%)** |
| 아이템 합보다 큰 경우 | 36,904 |
| 아이템 합보다 작은 경우 | **0** |

`total_price` 에는 옵션가가 들어 있고 `order_item.price` 에는 없기 때문에 생기는 차이다.
**작은 경우가 하나도 없다**는 점이 이를 뒷받침한다.

### 3-4. 결제

| 확인 | 결과 |
|---|---|
| `payment.amount` = `orders.total_price` | **51,731 / 51,731 (100%)** |
| `order_item_option.price` = `opt_item.add_price` | **346,845 / 346,845 (100%)** |

고객 청구액과 옵션 단가는 완전히 정상이다.

## 4. 어긋난 금액 규모

| | |
|---|---|
| `total_price` ↔ 아이템 합 차이 절대합 | 151,534,600원 |
| 주문 한 건당 최대 차이 | 23,800원 |

이 금액은 **덜 받았거나 더 받은 돈이 아니다.** `order_item.price` 에 옵션가가 빠져 생긴 장부상
차이이며, 실제 청구는 `total_price` 기준으로 이뤄졌다.

## 5. 고칠 수 있는 범위

불일치 주문 36,904건 중 **재계산으로 맞출 수 있는 것은 30,318건(82%)** 이다.

```sql
-- 재계산 값 (실행하지 말 것, 계산만)
SELECT oi.id, oi.price AS 현재, m.price + COALESCE(opt.s, 0) AS 재계산
FROM order_item oi
JOIN menu m ON m.id = oi.menu_id
LEFT JOIN (SELECT order_item_id, SUM(price*quantity) s FROM order_item_option
           GROUP BY order_item_id) opt ON opt.order_item_id = oi.id
WHERE oi.price <> m.price + COALESCE(opt.s, 0);
```

나머지 6,586건은 재계산해도 `total_price` 와 맞지 않는다. 원인은 확인하지 못했다.

## 6. 재계산이 위험한 이유

**`menu.updated_at > orders.created_at` 인 아이템이 81,985개다.** 전체 83,190개 중 거의 전부다.

메뉴 가격이 주문 이후에 바뀌었다면, 지금의 `menu.price` 로 과거 주문의 단가를 다시 계산하는 것은
**주문 시점의 청구 기록을 현재 가격으로 덮어쓰는 일**이 된다. 3절의 수치들도 이 한계를 안고 있다
— "규칙 일치"를 현재 메뉴가로 판정했기 때문이다.

가격 변경 이력을 남기는 테이블이 없어서, 주문 시점의 메뉴 가격을 복원할 방법이 현재로선 없다.

## 7. 권고

**`order_item.price` 를 재계산하지 않는 쪽을 권한다.**

- 고객 청구액(`total_price`, `payment.amount`)은 정확하다. 실질 피해가 없다.
- 깨진 데이터는 전부 시드이며, 실제 운영 데이터(2026-08~)는 100% 정상이다.
- 재계산은 주문 시점 가격이 아니라 현재 가격을 쓰게 되어, 정확하지 않은 기록을 정확해 보이게
  만들 뿐이다.

대신 다음을 권한다.

1. **인기 메뉴 매출 통계가 실제로 영향을 받고 있다 — 확인 필요.** (아래 7-1)
2. **시드를 다시 만들 계획이 있으면 그때 규칙에 맞춰 생성한다.** 지금 데이터를 고치는 것보다
   생성 단계를 고치는 편이 안전하다.
3. **주문 시점 메뉴 가격 보존을 검토한다.** `order_item` 에 `menu_price` 같은 스냅샷 컬럼이 있으면
   이런 검증이 가능해지고, 가격 변경 뒤에도 과거 주문을 정확히 재현할 수 있다.

### 7-1. 매출 뷰 점검 결과

어떤 뷰가 무엇을 합산하는지 확인했다.

| 뷰 | 합산 대상 | 영향 |
|---|---|---|
| `vw_sales_daily` | `payment.paid_at` 기준 결제 금액 | **정상** — 결제액은 정확하다 |
| `vw_sales_hourly` | 〃 | **정상** |
| `vw_order_summary` | `orders.total_price` | **정상** |
| `vw_top_menu_daily` | `SUM(oi.price * oi.quantity)` | **옵션 매출 누락** |
| `vw_top_menu_hourly` | 〃 | **옵션 매출 누락** |

**매출 총액 지표는 안전하다.** `vw_sales_*` 는 결제 금액을 쓰기 때문이다.

문제는 인기 메뉴 통계다. `order_item.price` 를 합산하므로 옵션 추가금이 빠진다.

| | 금액 |
|---|---|
| 현재 집계 | 797,250,600원 |
| 옵션 포함 시 | 960,687,000원 |
| 차이 | **163,436,400원 (약 17% 과소)** |

메뉴별 순위 자체는 크게 흔들리지 않겠지만(모든 메뉴가 같은 방향으로 과소집계된다),
**"메뉴별 매출액"을 절대 금액으로 읽으면 안 된다.** 옵션 매출이 통째로 빠진 수치다.

다만 이는 시드 데이터 때문이며, 2026-08 이후 쌓이는 실제 주문은 규칙을 지키므로 시간이 지나면
자연히 정확해진다. `vw_top_menu_*` 를 고쳐 옵션가를 포함시킬지는, 시드 데이터를 남겨둘지와 함께
판단할 문제다.

## 8. 재조사 방법

핵심 지표를 한 번에 보는 쿼리다.

```sql
SELECT
  COUNT(*) AS 아이템수,
  SUM(oi.price = m.price + COALESCE(opt.s, 0)) AS 규칙일치,
  SUM(opt.s > 0) AS 옵션있음,
  SUM(opt.s > 0 AND oi.price = m.price) AS 옵션누락
FROM order_item oi
JOIN menu m ON m.id = oi.menu_id
LEFT JOIN (SELECT order_item_id, SUM(price*quantity) s FROM order_item_option
           GROUP BY order_item_id) opt ON opt.order_item_id = oi.id;
```

시기별로 보려면 `orders` 를 조인해 `DATE_FORMAT(o.created_at, '%Y-%m')` 으로 묶는다.
**시드와 실제 주문을 반드시 나눠서 봐야 한다.** 전체 비율만 보면 실제 데이터의 문제를 놓친다
(중복 옵션 통합 때 실제로 그렇게 놓쳤다).
