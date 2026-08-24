# 매출 조회(getSalesSummary 등) 응답속도 개선 방법

- 작성일: 2026-08-21
- 대상: `GET /api/admin/sales/summary`, `/sales/daily`, `/sales/monthly`
- 상태: **개선 1은 이미 다른 작업에서 적용돼 있었음(이 문서 작성 중 확인). 개선 2~5는 미적용**

> **2026-08-21 정정**
> 이 문서를 쓰는 동안 `ASAK-back`의 HEAD가 다른 작업으로 갱신됐다. 조사 시점에 읽은 워킹트리는
> 옛 버전이었고, 현재 HEAD(`ec83c11`)에는 **개선 1(KPI 전 기간 조회 4회 → 1회)과 버그 1 가드가
> 이미 반영돼 있다** — `buildSummaryKpis()`, `beforeRange()`, `getSalesTotals` 모두 존재.
> 아래 "개선 1" 절은 이미 적용된 내용의 설명으로 읽으면 된다.
>
> 다만 그 과정에서 **`AdminSalesMapper.xml`에 `getSalesTotals` select가 2개 중복 정의**돼 있었다
> (44줄 `vw_sales_daily` 버전, 317줄 베이스 테이블 버전). MyBatis는 같은 namespace에 같은 id가
> 중복되면 기동 시 `Mapped Statements collection already contains value for ...`로 실패한다.
> **더 빠른 베이스 테이블 버전을 남기고 `vw_sales_daily` 버전을 삭제했다.** 이것이 2026-08-21에
> 이 저장소에 실제로 반영된 유일한 변경이다.
>
> 남은 병목은 `getDailySalesRows`(`vw_sales_daily` 1회, ~1.2초)다. 이것까지 우회하려면
> 취소율·취소금액 계산식을 뷰와 동일하게 재현해야 해서 DB 실측·동등성 검증이 가능할 때 진행한다.

- 증상: 브라우저 DevTools Timing 기준 `Waiting for server response` **6.53초**
  (Queueing 2.2ms, Stalled 0.4ms, Content Download 0.41ms — 네트워크가 아니라 서버 처리 시간)

> 이 문서는 2026-08-20 대시보드 최적화 리포트(`../2026-08-20/dashboard-performance-optimization.md`)의
> "남은 것"에 적어둔 항목을 실제로 처리하기 위한 후속 문서다. 병목 원인과 우회 패턴은 그때
> 이미 실측으로 규명됐고, 매출 화면에 아직 적용되지 않은 상태다.

## 0. 실측 결과 (2026-08-21, 로컬 서버 + 실제 DB)

현재 소스로 서버를 재기동해서 각 엔드포인트를 3회씩 측정했다.

### 수정 전 (getSalesTotals 중복 상태로 기동돼 있던 서버)

| 엔드포인트 | 결과 |
|---|---|
| `/sales/summary` (period 무관 전부) | **HTTP 500** |
| `/sales/daily` | 200 / 1.96s |
| `/sales/monthly?year=2026` | 200 / 10.29s |

`/sales/summary`가 **전부 500으로 죽어 있었다.** Java(`buildSummaryKpis`)는 `getSalesTotals`를
호출하는데 실행 중이던 빌드의 매퍼 XML에서 해당 statement를 못 찾아 MyBatis가 예외를 던졌다.

### 수정 후 (getSalesTotals 중복 제거, 베이스 테이블 버전만 남김)

| 엔드포인트 | 1회 | 2회 | 3회 |
|---|---|---|---|
| `/dashboard` | 0.247s | 0.248s | 0.263s |
| `/sales/summary` (기본) | 2.530s | 2.732s | 2.536s |
| `/sales/summary?period=today` | 1.970s | 1.903s | 1.840s |
| `/sales/summary?period=week` | 2.572s | 2.407s | 2.459s |
| `/sales/summary?period=month` | 2.530s | 2.573s | 2.523s |
| `/sales/daily` | 2.049s | 1.957s | 2.173s |
| `/sales/monthly?year=2026` | **10.351s** | **10.362s** | **10.252s** |

**`/sales/summary`가 500 → 200으로 복구됐다.** 중복 제거가 실제 장애를 고친 것이 확인됐다.

### 개선 5 적용 후 (월별 랭킹을 선택한 달만 조회)

`/sales/monthly`에 `month` 파라미터를 추가하고, 랭킹을 12개월 전부가 아니라 **해당하는 달 하나만**
조회하도록 바꿨다(화면이 실제로 쓰는 건 선택한 달 하나뿐이다).

| 요청 | 1회 | 2회 | 3회 |
|---|---|---|---|
| `?year=2026` (month 미지정 → 이번 달) | 4.032s | 3.855s | 3.636s |
| `?year=2026&month=8` | 3.742s | 4.125s | 3.908s |
| `?year=2026&month=3` | 3.701s | 5.146s | 3.856s |
| `?year=2025` (지난 연도 → 마지막 달) | 3.819s | 3.778s | 3.631s |

**10.3초 → 3.6~4.1초.**

응답 정확성도 확인했다.

- `month=8` → `ranking` 키가 `["2026-08"]` 하나만, 항목 4개
- `month=3` → `["2026-03"]`
- `month` 미지정 + 올해 → `["2026-08"]`(이번 달)
- `year=2025`(지난 연도) → `["2025-12"]`(rows의 마지막 달)
- `month=13`, `month=0` → `SALES_MONTH_INVALID`

`ranking` 응답 형태(월 키 → 행 목록)는 그대로라서 **프론트 화면 코드는 수정이 필요 없었다**
(`ranking?.[monthKey]`가 그대로 동작). `month`를 요청에 실어 보내도록 `salesApi`,
`useSalesQuery`, `MonthlySalesPage`만 연결했다.

> 트레이드오프: 이전에는 연 1회 조회로 모든 월 랭킹을 받아둬서 월 전환이 즉시였지만, 이제 월을
> 바꾸면 API를 다시 호출한다. 대신 첫 조회가 3배 가까이 빨라졌다.

### getMinYear() 제거 후 (최종)

`getMinYear()`가 요청당 1.5초를 먹고 있었다. 두 단계로 걷어냈다.

1. **뷰 우회** — `vw_sales_monthly`(= `vw_sales_daily`를 다시 롤업)에서 `MIN(year)`를 구하느라
   매출 전체를 두 번 집계하고 있었다. `vw_sales_daily`에는 **WHERE 절이 없어** orders 전체가
   그대로 들어가므로, 베이스 테이블에서 같은 식으로 최소 연도를 구하면 결과가 같다.
   → 1.5초 → 0.82초
2. **캐시** — 이 값은 "가장 오래된 주문의 연도"라 새 주문이 쌓여도 바뀌지 않는다.
   `AdminSalesService`에 `volatile int cachedMinYear`로 memoize.
   → 0.82초 → **0.003초**

```
getMinYear 경로 실측
  수정 전 : 1.571s / 1.598s / 1.518s
  뷰 우회 : 0.822s / 0.800s / 0.837s
  캐시 후 : 0.0027s / 0.0031s / 0.0025s
```

**정확성 검증**: 수정 전후로 `minYear` 경계가 동일하다(`year=2024` → `SALES_YEAR_INVALID`,
`year=2025` → 성공). 세 단계 모두에서 같은 결과를 확인했다.

### 최종 측정 (2026-08-21)

| 엔드포인트 | 1회 | 2회 | 3회 |
|---|---|---|---|
| `/dashboard` | 0.241s | 0.260s | 0.238s |
| `/sales/summary` | 2.582s | 2.527s | 2.650s |
| `/sales/summary?period=today` | 1.801s | 1.971s | 2.001s |
| `/sales/summary?period=week` | 2.476s | 2.434s | 2.380s |
| `/sales/summary?period=month` | 2.538s | 2.617s | 2.562s |
| `/sales/daily` | 2.036s | 1.939s | 1.948s |
| `/sales/monthly?year=2026` | 2.247s | 2.026s | 2.107s |

`/sales/monthly` **10.3초 → 2.0~2.2초 (약 5배)**, `/sales/summary`는 **500 에러 → 2.5초**.

### 남은 문제

1. ~~**`getMinYear()`가 요청당 1.5초를 먹는다.**~~ → **해결됨(위 절 참조).** 이하 원문 보존:
   **`getMinYear()`가 요청당 1.5초를 먹는다.** `AdminSalesController.getMonthlySales()`가 연도
   유효성 검사를 위해 매 요청 호출하는데, `SELECT COALESCE(MIN(year), ...) FROM vw_sales_monthly`라
   무거운 뷰를 통째로 스캔한다. 실측(연도 검증에서 바로 반환되는 경로): **1.51~1.60초**.
   `/sales/monthly`에 남은 3.7초 중 1.5초가 이것이다. 값이 거의 변하지 않으므로 캐시하거나
   `orders`에서 직접 구하면 바로 걷어낼 수 있다.
2. `getMonthlySalesRows`(`vw_sales_monthly`)가 남은 ~2.2초의 대부분으로 보인다.
   `vw_sales_monthly`는 `vw_sales_daily`를 롤업한 뷰라 같은 병합 불가 문제를 물려받는다.
2. `/sales/summary` 2.5초, `/sales/daily` 2.0초 — 개선 2 잔여(`getDailySalesRows`의
   `vw_sales_daily` 1회, ~1.2초)와 share/ranking 쿼리가 남아 있다.
3. 최초 스크린샷의 **6.53초가 어느 엔드포인트였는지는 확정하지 못했다.** 측정 시점 기준으로
   `/sales/summary`는 2.5초, `/sales/monthly`는 10.3초다.

### 측정 환경 메모

- `.env`가 CRLF 줄바꿈이라 셸에서 그대로 `export`하면 값 끝에 ``이 붙어
  `Access denied for user`로 DB 인증이 실패한다. 값 끝의 ``을 제거하고 주입해야 한다.
- 기동 전 `build/resources`가 잠겨 `processResources`가 `Failed to clean up stale outputs`로
  실패할 수 있다. 해당 디렉터리를 지우고 재시도하면 된다.

## 1. 원인

`AdminSalesService.getSalesSummary()`가 한 번의 요청에서 `vw_sales_daily`를 **5번** 탄다.

| 호출 지점 | Mapper | 경유 뷰 |
|---|---|---|
| `getDailySalesRows(range)` | 일별 행 | `vw_sales_daily` |
| `kpi("총매출")` | `getDailySales(before)` | `vw_sales_daily` |
| `kpi("주문 수")` | `getDailyOrderCount(before)` | `vw_sales_daily` |
| `kpi("평균 객단가")` | `getDailySales` + `getDailyOrderCount` | `vw_sales_daily` ×2 |
| `getPaymentShare(range)` | 결제수단 비중 | `vw_order_summary` ×2 스캔(메인+서브쿼리) |
| `getOrderShare(range)` | 주문유형 비중 | `vw_order_status_summary` ×2 스캔 |
| `getMenuRankingByRange(range)` | 메뉴 랭킹 | `vw_top_menu_daily` |

`vw_sales_daily`는 `GROUP BY DATE(COALESCE(p.paid_at, o.created_at))`를 갖고 있어서 MySQL이
뷰를 병합(merge)하지 못한다. 바깥에서 `WHERE sales_date BETWEEN ...`을 걸어도 **orders 전체
이력(5만여 건)을 먼저 통째로 집계한 뒤에** 필터링한다. 2026-08-20 실측으로 **1회당
약 1.2~1.4초**가 기록돼 있다(`AdminSalesMapper.xml`의 `getDashboardKpi` 주석 참조).

**1.2초 × 5회 ≈ 6초** — 관측된 6.53초와 일치한다.

추가로, KPI 3장이 쓰는 `before`(전 기간) 범위는 **셋 다 완전히 동일**하다. 같은 값을 4번
다시 계산하고 있다.

### 다른 엔드포인트

- `/sales/daily` — `getDailySalesRows`(`vw_sales_daily`) 1회 + 랭킹·결제비중·주문비중 3회 = 4회 왕복
- `/sales/monthly` — `getMonthlySalesRows` 1회 + **월마다 `getMenuRankingByRange`를 따로 호출**해
  최대 12회 추가 (전형적인 N+1). 총 최대 13회 왕복

## 2. 개선안 (효과 큰 순서)

### 개선 1 — KPI 전 기간 조회 4회 → 1회

`AdminSalesService.kpi()`가 KPI 라벨별로 매번 DB를 다시 친다. 셋 다 같은 `before` 범위이므로
호출부에서 한 번만 조회해 세 KPI가 공유하면 된다.

Mapper에 합산 쿼리를 하나 추가한다.

```xml
<select id="getSalesTotals" resultType="map">
  SELECT
    COALESCE(SUM(net_sales_amount), 0) AS netSales,
    COALESCE(SUM(order_count), 0)      AS orderCount
  FROM vw_sales_daily
  WHERE sales_date BETWEEN #{startDate} AND #{endDate}
</select>
```

`getSalesSummary()`에서 `before` 범위를 한 번만 계산해 `getSalesTotals(before)`를 1회 호출하고,
그 값으로 `buildKpiResponse()`를 세 번 조립한다(`buildKpiResponse`는 DB를 치지 않는 조립
전용 메서드로 2026-08-20에 이미 분리해 뒀다).

**SQL 튜닝 없이 이것만으로 4회 → 1회, 약 3.6초 절감.**

### 개선 2 — `vw_sales_daily` 우회

2026-08-20에 `getDashboardKpi`/`getDashboardWeeklySales`에 적용해 **결과값이 뷰 기준과 완전
일치함을 검증한** 패턴을 그대로 쓴다. 뷰 정의는 건드리지 않고 Mapper 쿼리만 베이스 테이블을
직접 집계하도록 바꾼다.

핵심은 MySQL이 조인 전에 행을 줄일 수 있도록 **`o.created_at` 범위를 ±1일 여유를 두고 먼저
걸고**, 정확성을 위해 `DATE(COALESCE(p.paid_at, o.created_at))` 조건을 함께 유지하는 것이다
(자정 넘어 결제되는 주문 대비).

```sql
SELECT
  DATE(COALESCE(p.paid_at, o.created_at)) AS sales_date,
  COUNT(DISTINCT CASE
          WHEN p.paid_at IS NOT NULL AND os.code <> 'CANCELED'
               AND ps.code NOT IN ('CANCELED','REFUNDED') THEN o.id
        END) AS order_count,
  (COALESCE(SUM(CASE WHEN p.paid_at IS NOT NULL THEN p.amount ELSE 0 END), 0)
   - COALESCE(SUM(CASE
                    WHEN p.paid_at IS NOT NULL
                         AND (os.code = 'CANCELED' OR ps.code IN ('CANCELED','REFUNDED'))
                    THEN p.amount ELSE 0
                  END), 0)) AS net_sales_amount
FROM orders o
LEFT JOIN payment p      ON p.order_id = o.id
LEFT JOIN common_code ps ON ps.id = p.status_id
LEFT JOIN common_code os ON os.id = o.status_id
WHERE o.created_at >= DATE_SUB(#{startDate}, INTERVAL 1 DAY)
  AND o.created_at <  DATE_ADD(#{endDate},   INTERVAL 1 DAY)
  AND DATE(COALESCE(p.paid_at, o.created_at)) BETWEEN #{startDate} AND #{endDate}
GROUP BY DATE(COALESCE(p.paid_at, o.created_at))
```

적용 대상: `getDailySalesRows`, `getDailySales`, `getDailyOrderCount`(개선 1의 `getSalesTotals`).

**회당 1.2초 → 약 90ms.**

> 개선 1 + 2를 합치면 뷰 경유 5회(~6초)가 베이스 테이블 2회(~0.2초)가 된다.
> **6.5초 → 0.5초 안팎**으로 예상한다. (예상치 — 실측 필요)

### 개선 3 — `orders.created_at` 단독 인덱스 추가

현재 `orders`의 인덱스는 다음과 같다.

```
PRIMARY KEY (id)
UNIQUE KEY order_no (order_no)
KEY fk_orders_type (order_type_id)
KEY idx_orders_status_created_at (status_id, created_at)
```

`created_at` **단독 인덱스가 없다.** 복합 인덱스는 선행 컬럼(`status_id`) 조건이 없으면 range
스캔에 쓰지 못하므로, 개선 2의 우회 쿼리들처럼 `WHERE o.created_at BETWEEN ...`만 거는 경우
인덱스를 타지 못한다. **2026-08-20에 적용한 대시보드 우회 쿼리들도 지금 orders를 풀스캔하고
있을 가능성이 크다.**

```sql
CREATE INDEX idx_orders_created_at ON orders (created_at);
```

`getDashboardRecentOrders`의 `ORDER BY o.created_at DESC LIMIT 3`(현재 filesort)도 같이
개선된다. `payment.paid_at`도 단독 인덱스가 없으니 EXPLAIN 결과를 보고 판단한다.

> DDL 변경이므로 적용 전 EXPLAIN으로 효과를 확인하고, 쓰기 성능 영향(주문 INSERT)도 같이 본다.

### 개선 4 — share 쿼리 스캔 반감

`getPaymentShare`/`getOrderShare`가 백분율 분모를 **서브쿼리로 다시 구해서 같은 뷰를 두 번**
읽는다. 윈도우 함수로 1회 스캔으로 줄인다.

```sql
SELECT
  payment_method_name AS label,
  ROUND(COUNT(*) / NULLIF(SUM(COUNT(*)) OVER (), 0) * 100, 2) AS percent
FROM vw_order_summary
WHERE DATE(paid_at) BETWEEN #{startDate} AND #{endDate}
  AND payment_status_code NOT IN ('CANCELED','REFUNDED')
GROUP BY payment_method_name
ORDER BY percent DESC, label
```

`getOrderShare`도 `SUM(SUM(order_count)) OVER ()`로 동일하게 처리한다.

### 개선 5 — 월별 매출 N+1 제거 (2026-08-21 적용 완료 — 0절 참조)

`getMonthlySales()`가 월별 행을 받아 **월마다 `getMenuRankingByRange`를 반복 호출**한다.
연 단위 랭킹을 한 방에 뽑고 Java에서 월별로 그룹핑한다.

```sql
SELECT
  DATE_FORMAT(sales_date, '%Y-%m') AS month,
  menu_id AS menuId, menu_name AS menuName,
  SUM(order_count) AS orderCount, SUM(sales_amount) AS salesAmount
FROM vw_top_menu_daily
WHERE sales_date BETWEEN #{startDate} AND #{endDate}
GROUP BY month, menu_id, menu_name
```

Service에서 월별로 묶고 `salesAmount DESC, menuId` 정렬 후 상위 4개만 남긴다(기존 SQL의
`LIMIT 4`와 `rank` 부여 규칙을 Java 쪽에서 동일하게 재현할 것).

**최대 13회 → 2회 왕복.**

## 3. 함께 발견한 버그 2개

성능과 별개로 `AdminSalesService.kpi()`에 실제 결함이 있다.

### 버그 1 — 0으로 나누기로 500 에러

```java
beforeValue = adminSalesMapper.getDailySales(before)
            / adminSalesMapper.getDailyOrderCount(before);
```

전 기간 주문이 0건이면 `ArithmeticException: / by zero`가 난다. 시연에서 과거 데이터가
없는 기간을 고르면 바로 터진다. 개선 1에서 조회를 통합할 때 분모 0 가드를 같이 넣는다.

### 버그 2 — 전 기간 범위가 하루 짧다

```java
long size = endDate.toEpochDay() - startDate.toEpochDay();
before.put("startDate", startDate.minusDays(size + 1));
before.put("endDate",   startDate.minusDays(1));
```

원 범위 길이는 `size + 1`일인데 `before` 범위는 `size`일이다. 비교 대상 기간이 하루 짧아
델타(%)가 부정확하다. 원 범위 일수(`size + 1`)만큼 통째로 앞당기도록 정리해야 한다.

## 4. 적용 순서 제안

1. 개선 1 (Service·Mapper만, 위험 낮음, 효과 큼)
2. 버그 1 가드 (개선 1과 같은 자리)
3. 개선 2 (2026-08-20 검증된 패턴 재사용)
4. **여기서 실측** — 목표치(1초 이내) 도달했는지 확인
5. 미달이면 개선 3(인덱스) → EXPLAIN 확인 후 적용
6. 개선 4·5는 해당 화면(일별/월별)이 실제로 느릴 때
7. 버그 2는 성능과 무관하므로 별도 처리

## 5. 검증 방법

- 각 Mapper 호출에 `System.currentTimeMillis()`를 임시로 찍어 병목을 특정한다(측정 후 제거).
  2026-08-20에 이 방식으로 범인을 정확히 잡았다.
- **결과값 동등성 검증이 필수다.** 뷰 우회 쿼리와 기존 뷰 기준 결과를 같은 기간에 대해
  전건 비교한다(2026-08-20에는 최근 500건/전체 일자 비교로 완전 일치를 확인했다).
- **뷰를 재작성해서 재는 경우, 반드시 실제 `CREATE VIEW`로 만든 뷰를 대상으로 측정한다.**
  같은 SQL을 raw 쿼리로 재면 옵티마이저가 다르게 동작한다(2026-08-20 4단계에서 이걸 놓쳐
  1.3초 → 6.7초로 오히려 느려진 전례가 있다). 이번 개선안은 전부 **뷰 미변경**이라 이 함정은
  피해 간다.

## 6. 미확인 사항

- 이 문서의 수치는 **코드 구조 분석 + 2026-08-20 실측 기록**에 근거한 추정이다.
  작성 시점에 `DB_URL` 환경변수가 없어 직접 실측하지 못했다.
- 6.53초가 찍힌 요청이 `/sales/summary`인지 스크린샷의 URL로 확정하지 못했다. DB 왕복 횟수와
  뷰 1회당 소요시간을 곱한 값이 가장 잘 맞는 것은 `/sales/summary`다.
- `vw_order_summary`, `vw_order_status_summary`, `vw_top_menu_daily`의 실제 소요시간은
  측정된 바 없다. 개선 1·2 적용 후에도 느리면 이쪽을 프로파일링한다.
