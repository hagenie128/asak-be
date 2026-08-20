# 관리자 대시보드(getDashboard()) 응답속도 개선

- 작업일: 2026-08-20
- 대상: `GET /api/admin/dashboard`
- 결과: **8초 → 0.43~0.50초** (약 16~18배)

## 단계별 진행

### 1단계 — DB 호출 횟수 축소 (8초 → 3.7~3.9초)

`getDashboard()`가 호출당 DB를 약 10회 왕복하고 있었다. 특히 KPI 카드 4개(오늘 매출/주문
수/평균 객단가/진행 중 주문)가 "전일 대비" 델타 계산을 위해 각자 `getDailySales`/
`getDailyOrderCount`를 따로 호출했고, "평균 객단가"는 그 둘을 다 호출해 2회, 심지어
"진행 중 주문"도 label에 "주문"이 포함돼 있어 `getDailyOrderCount`를 불필요하게 한 번 더
호출하는 기존 버그(성능 이슈지 정답이 바뀌는 버그는 아니라 그대로 유지)까지 있었다.

- `AdminSalesMapper.getDashboardKpi(date)` → `getDashboardKpi(today, yesterday)`로 바꿔
  오늘·전일 값을 한 번에 조회
- `AdminSalesService`의 delta·표시 포맷 로직을 `buildKpiResponse()`로 분리해서 `getDashboard()`는
  이미 가져온 값만 넘기고, `getSalesSummary()`가 쓰는 기존 `kpi()`(기간별 조회, DB 호출 필요)는
  그대로 유지
- 결과: DB 호출 10회 → 6회, 8초 → 3.7~3.9초

### 2단계 — 개별 Mapper 호출 프로파일링

각 Mapper 호출에 `System.currentTimeMillis()`를 임시로 찍어서 범인을 특정했다(측정 후 코드는
제거):

| 호출 | 소요 시간 |
|---|---|
| kpi (`vw_sales_daily` 2회분) | ~1,200~1,400ms |
| recentOrders (`vw_order_list_summary`) | ~1,280~1,360ms |
| weeklySales (`vw_sales_daily` 7일분) | ~940~970ms |
| orderType | ~80ms |
| statusSummary | ~80ms |
| inventory | ~6ms |

### 3단계 — `vw_sales_daily` 우회 (kpi, weeklySales)

`vw_sales_daily`는 `GROUP BY DATE(COALESCE(paid_at, created_at))`가 있어서 MySQL이 뷰를
병합하지 못하고, 바깥에서 `WHERE sales_date = ...`를 걸어도 **orders 전체 이력(당시 5만
1천여 건, 2025-03-01부터)을 먼저 통째로 집계한 뒤에** 필터링한다. 뷰는 파라미터를 받을 수
없어서 뷰 자체를 고칠 수 없었고, 대신 `getDashboardKpi`/`getDashboardWeeklySales` 두 매퍼
쿼리가 뷰를 거치지 않고 `orders`/`payment`를 직접 조회하도록 바꿨다.

- 정확한 결과를 위해 `DATE(COALESCE(p.paid_at, o.created_at))` 조건은 그대로 유지하되,
  MySQL이 조인 전에 행을 줄일 수 있도록 `o.created_at` 범위(±1일 여유, 자정 넘어 결제되는
  경우 대비)를 먼저 건다. `o.created_at`만 걸면 조인 없이 걸러지지만 paid_at이 다른 날일 수
  있는 극단적 경우를 놓치므로, 여유를 둔 created_at 범위 + 정확한 COALESCE 조건 두 개를
  같이 쓴다.
- `activeOrderCount`도 `(SELECT COUNT(*) FROM vw_order_live)`였는데, `vw_order_live`는
  주방보드용 JSON 상세 뷰라 COUNT만 필요해도 전체 JSON 집계를 다 계산했다(~400ms). 단순
  `orders JOIN common_code WHERE status IN ('RECEIVED','PREPARING','READY')` 카운트로 교체
  (~6ms).
- 검증(2026-08-20, 실제 DB): 기존 뷰 기준 결과값과 완전 일치. kpi ~1,200~1,400ms → ~80ms,
  weeklySales ~940~970ms → ~90ms.
- 결과: 3.7~3.9초 → 1.6~1.7초

### 4단계 — `vw_order_list_summary` 재작성 시도 → 실패 → 원복

recentOrders(~1.3초)의 원인은 뷰가 주문마다 `GROUP_CONCAT`로 요약을 만드는 파생 테이블을
**order_item 전체(8만 3천여 행)에 대해 먼저 계산한 뒤** 정렬·3건만 잘라내는 구조였다.

파생 테이블 JOIN을 상관 서브쿼리로 바꾸면 뷰에 최상위 GROUP BY가 없어져 "병합 가능"해지고,
`ORDER BY created_at DESC LIMIT 3`이 뷰 안으로 밀려 들어가 3건에 대해서만 서브쿼리가 돌
것으로 예상하고 `CREATE OR REPLACE VIEW`로 실제 적용했다.

**하지만 오히려 1.3초 → 6.6~6.8초로 5배 느려졌다.** EXPLAIN으로 확인한 결과, MySQL은
**SELECT 목록에 상관 서브쿼리가 있는 뷰는 병합하지 않고 여전히 TEMPTABLE로 통째로
구체화**한다 — GROUP BY 유무와 무관한 별도 제약이었다. 사전 검증 때 이 부분을 실제
`CREATE VIEW`가 아니라 같은 SQL을 raw 쿼리로 실행해서 쟀는데(정확성 검증은 800건 비교로
실제 뷰에 대해 제대로 했지만, 속도 검증은 raw 쿼리 기준이었음), raw 쿼리와 뷰로 감싼
경우 MySQL 옵티마이저가 다르게 동작한다는 걸 놓쳤다.

→ 즉시 원래 정의(`GROUP_CONCAT` 파생 테이블 방식)로 원복했다. `vw_order_list_summary`는
**지금 운영 DB에 2026-08-20 이전과 동일한 정의**로 남아 있다.

### 5단계 — `vw_order_list_summary`를 우회 (recentOrders)

4단계에서 배운 대로, "뷰로 감싸면 병합이 안 되지만 raw 쿼리로 직접 쓰면 정상 최적화된다"는
점을 활용해 **뷰 자체는 손대지 않고**, `getDashboardRecentOrders` 매퍼 쿼리만 뷰를 거치지
않고 `orders`/`order_item`/`menu`를 상관 서브쿼리로 직접 조회하도록 바꿨다. 대시보드 카드가
필요한 컬럼만 쓰므로 `payment`/`payment_status` 조인도 아예 뺐다.

- 검증(2026-08-20, 실제 DB, raw 쿼리 기준): 최근 500건 결과 기존 뷰 기준과 완전 일치,
  ~1.3초 → ~90ms.
- 결과: 1.6~1.7초 → **0.43~0.50초**

## 최종 반영 파일

- `src/main/java/com/asak/admin/mapper/AdminSalesMapper.java` — `getDashboardKpi`
  시그니처 `(today, yesterday)`로 변경
- `src/main/resources/mappers/AdminSalesMapper.xml` — `getDashboardKpi`,
  `getDashboardWeeklySales`, `getDashboardRecentOrders` 3개 쿼리를 뷰 우회 버전으로 교체
  (뷰 자체는 미변경)
- `src/main/java/com/asak/admin/service/AdminSalesService.java` — `getDashboard()`가
  통합된 kpi 값을 그대로 쓰도록 정리, delta 계산 로직을 `buildKpiResponse()`로 분리
- `src/main/java/com/asak/admin/controller/AdminSalesController.java` — 별개로 발견된
  `getSalesSummary()` 호출 인자 불일치(다른 작업과의 동시 편집으로 생긴 문제)도 같이 수정

## DB에는 결과적으로 아무 변경 없음

`vw_order_list_summary`는 재작성 시도 후 원복해서 **2026-08-20 이전과 동일한 정의**다.
`vw_sales_daily`, `vw_order_live`도 전혀 건드리지 않았다. 이번 개선은 전부 애플리케이션
코드(Mapper SQL) 쪽 변경이다.

## 남은 것 / 참고

- `getDailySalesRows`/`getHourlySalesByRange` 등 다른 화면(월별/일별 매출 리포트)도
  `vw_sales_daily`를 쓰지만, 이번 범위는 대시보드로 한정했다. 그 화면들도 느리면 같은
  패턴(베이스 테이블 직접 조회)을 적용할 수 있다.
- `AdminOrderMapper.getOrderList`/`countOrderList`(TODO-007, 아직 미구현·미배선)는
  `vw_order_list_summary`를 그대로 쓰므로, 나중에 그 기능을 실제로 붙일 때 목록 규모에
  따라 이번과 같은 방식(뷰 우회)이 필요할 수 있다.
- 교훈: **뷰를 재작성해서 속도를 검증할 때는 반드시 실제 `CREATE VIEW`로 만든 뷰를 대상으로
  측정해야 한다.** 같은 SQL을 raw 쿼리로 재는 것과 결과가 다를 수 있다(이번처럼 상관
  서브쿼리가 있으면 특히 그렇다).
