# 중복 옵션 정리 — 통합 계획과 실행 기록

> 작성일: 2026-08-19
> 대상: `opt_item`, `opt_policy_item`, `order_item_option`
> 상태: **2026-08-19 실행 완료. 검증 통과.** 결과는 0-1절 참고.
> 배경 조사: [pdf-db-gap-2026-08-19.md](pdf-db-gap-2026-08-19.md) 1절

## 0-1. 실행 결과 (2026-08-19)

계획대로 실행했고 검증을 모두 통과했다.

| 테이블 | 전 | 후 | 기대 |
|---|---|---|---|
| `opt_item` | 157 | **115** | 115 |
| `opt_policy_item` | 734 | **626** | 626 |
| `order_item_option` | 349,157 | **346,845** | 346,845 |

무결성 검증 5종 모두 0 — 그룹 내 이름 중복, 메뉴·정책별 옵션 중복, 끊긴 주문옵션 참조,
끊긴 정책연결 참조, 옵션 없는 정책.

**금액 불변 확인:** `orders.total_price` 합계가 948,785,200 으로 작업 전과 같다.

주문 이력은 남길 id 로 정확히 합쳐졌다.

| 옵션 | 통합 전 (남길 id 만) | 통합 후 |
|---|---|---|
| 견과류 #320 | 1,871 | 3,658 |
| 그린밀싹 #351 | 3,836 | 7,644 |
| 단호박크림스프 #341 | 5,098 | 10,169 |

사라진 2,312행은 전부 한 주문에 두 옵션이 함께 담겼던 충돌분이며, 백업과 대조해도 그 외 손실은 없다.

재발 방지 제약 `uq_opt_item_group_name (opt_group_id, name)` 도 걸었다. 이제 같은 그룹에 같은
이름을 두 번 넣을 수 없다. `docs/아삭_mysql.sql` 도 이 변경을 반영해 재생성했다.

### 남긴 백업

| 테이블 | 행수 |
|---|---|
| `backup_20260819_opt_item_before_merge` | 157 |
| `backup_20260819_opt_policy_item_before_merge` | 734 |
| `backup_20260819_order_item_option_before_merge` | 349,157 |
| `backup_20260819_opt_dedupe_map` | 42 (keep_id ↔ drop_id 매핑) |

마지막 것은 계획서 5-5 에서 `DROP` 하기로 했던 `tmp_opt_dedupe_map` 을 이름만 바꿔 남긴 것이다.
**어느 옵션이 어디로 합쳐졌는지에 대한 유일한 기록**이라 지우지 않았다. `backup_` 접두어라
스키마 문서 대상에서도 자동 제외된다.

### 0-2. 부작용과 보정 — 계획의 오판 하나

계획서 4절에서 충돌 2,312행을 삭제(A안)하기로 하면서 **"금액에 영향 없음"이라고 했는데 틀렸다.**

그 판단은 `order_item.price` 에 옵션 추가금이 반영돼 있지 않다는 관찰(98.6%)에 근거했다. 그러나
그 98.6% 는 시드 데이터가 만든 수치였고, **2026-08 의 실제 주문은 옵션을 제대로 반영하고 있었다.**
충돌 행을 지우자 그 주문들만 `price` 와 옵션 합이 어긋났다.

| | 규칙 일치 |
|---|---|
| 통합 전 | 33,932건 |
| 통합 직후 | 33,895건 (**-37**) |
| 보정 후 | 33,932건 (복구) |

**보정 방법**: 삭제된 41행의 수량을 남은 id 행에 합산했다(계획서 4절 B안을 실제 주문에만 적용).
고객이 화면 중복 때문에 같은 토핑을 두 번 담았고 그만큼 청구됐으므로, 주문 이력은 청구 사실에
맞추는 것이 옳다고 판단했다.

```sql
UPDATE order_item_option cur
JOIN (SELECT bk.order_item_id AS oi_id, m.keep_id AS keep_id, SUM(bk.quantity) AS add_qty
      FROM backup_20260819_order_item_option_before_merge bk
      LEFT JOIN order_item_option c2 ON c2.id = bk.id
      JOIN backup_20260819_opt_dedupe_map m ON m.drop_id = bk.opt_item_id
      JOIN order_item oi ON oi.id = bk.order_item_id
      JOIN orders o ON o.id = oi.order_id
      WHERE c2.id IS NULL AND o.created_at >= '2026-08-01'
      GROUP BY bk.order_item_id, m.keep_id) t
  ON cur.order_item_id = t.oi_id AND cur.opt_item_id = t.keep_id
SET cur.quantity = cur.quantity + t.add_qty;
-- 41행. 백업: backup_20260819_oio_before_qty_fix
```

검증 결과 2026-08 아이템 1,962개 전부 규칙에 맞고(100%), 실제 주문의 옵션 금액 합
3,036,000원이 통합 전과 같아졌다. 주문 총액과 중복 제거 결과도 그대로다.

시드 주문(2026-08 이전) 2,219개는 보정하지 않았다. 원래부터 `price` 에 옵션이 반영돼 있지 않아
수량을 복원해도 규칙에 맞지 않고, 가짜 데이터를 건드리는 셈이기 때문이다.

> **교훈**: "영향 없음"을 전체 비율로 판단하면 안 된다. 시드와 실제 데이터가 섞인 테이블에서는
> **실제 데이터만 따로 떼어 확인**해야 한다. 이번엔 1.4% 가 실제 주문이었고 거기서 문제가 났다.

### 아직 안 한 것

계획서 8절의 두 가지는 그대로 남아 있다.

1. `UserMenuMapper.selectOptionItems` 에 `opi.active` 필터가 없다.
2. 고객 조회가 `menu_opt_override` 를 `COALESCE` 하지 않는다.

통합으로 화면 중복은 사라졌으므로 급하지는 않다.

## 1. 목표

`opt_item` 에 같은 옵션이 두 벌씩 있고 둘 다 같은 정책에 연결돼 있다. 고객 화면에 같은 토핑이
두 번 보이고, 관리자 목록도 중복이며, 통계도 두 id 로 쪼개져 집계된다.

**중복 42쌍을 하나로 합치고 여분 42행을 제거한다.** 화면에서 숨기는 것이 아니라 데이터를 바로잡는다.

| 항목 | 값 |
|---|---|
| 중복 쌍 | 42쌍 (전부 정확히 2개씩) |
| 제거할 `opt_item` | 42행 (157 → 115) |
| 이관할 `order_item_option` | **103,331행** (전체 349,154행 중) |
| 정리할 `opt_policy_item` | 108행 |
| UNIQUE 충돌 | **2,312건** — 별도 규칙 필요 (4절) |

## 2. 사전 확인 결과

실행 전에 확인해야 할 것들을 모두 조회했다. 결과는 전부 통합에 유리하다.

| 확인 | 결과 |
|---|---|
| 쌍의 속성이 실제로 같은가 (`add_price`, `list_price`, `amount`, `unit_id`, `ing_id`, `icon_url`, `color_hex`, `sold_out`) | **0쌍 차이** — 완전 동일 |
| `menu_opt_override` 가 제거 대상을 참조하는가 | **0행** (테이블 자체가 0행) |
| `opt_item_comp` 가 참조하는가 | **0행** |
| `opt_policy_item` 참조 | 108행 (정리 대상) |
| 쌍이 3개 이상인 경우 | 없음 (전부 2개) |
| 통합 후 남는 중복 (시뮬레이션) | **0건** |

### 금액에 미치는 영향 — 없음

옵션 행을 합치거나 지우면 주문 금액이 틀어질까 걱정했으나, 확인해 보니 그렇지 않다.

- `order_item_option.price` = `opt_item.add_price` — **349,154행 전부 일치.** 단가를 저장한다.
- `order_item.price` = `menu.price` — **83,189행 중 82,019행(98.6%).**
  즉 **옵션 추가금이 `order_item.price` 에 반영돼 있지 않다.**

따라서 `order_item_option` 을 어떻게 정리해도 `order_item.price` 와 `orders.total_price` 는
바뀌지 않는다.

> 별개 문제: 옵션 추가금이 주문 금액에 반영되지 않는 것 자체가 결함이다. `orders.total_price` 도
> `order_item` 합계와 51,749건 중 14,845건(28.7%)만 맞는다. 이 계획의 범위 밖이며 따로 다뤄야 한다.

## 3. 남길 쪽 판정

**작은 id 를 남기고 큰 id 를 제거한다.**

- 쌍 사이 유일한 차이는 `opt_policy_item.sort_no` 이고, **작은 id 가 항상 작은 sort_no** 다
  (견과류 #320→44 / #327→51). 작은 쪽을 남겨야 화면 정렬이 자연스럽다.
- `opt_item` 자체 속성은 완전히 동일하다(2절).
- 주문 수는 판정에 쓰지 않았다. 두 id 가 거의 반반이라(견과류 1,871 / 1,869) 어느 쪽도 "진짜"가
  아니며, 어차피 양쪽 주문 이력을 모두 남길 id 로 합치기 때문이다.

## 4. UNIQUE 충돌 처리 — 결정 필요

`order_item_option` 에 `UNIQUE(order_item_id, opt_item_id)` 가 있다. 같은 주문 아이템에 남길 id 와
제거할 id 가 **둘 다 담긴 경우가 2,312건** 있다. 그대로 UPDATE 하면 `Duplicate entry` 로 실패한다.

화면에 같은 토핑이 두 줄로 보였으니 둘 다 선택된 것이다. 샘플은 전부 `quantity=1, price` 동일이다.

| 방식 | 결과 | 판단 |
|---|---|---|
| **A. 제거할 행 삭제** | 「견과류 1개」로 남음 | 중복 노출로 인한 이중 선택은 오류지 의도가 아니다. **권장** |
| B. 수량 합산 | 「견과류 2개」가 됨 | 없던 주문 사실이 생긴다 |

금액에는 어느 쪽도 영향이 없다(2절). A 를 권장하지만 **업무 판단이 필요하다.**

## 5. 실행 순서

전 단계를 하나의 트랜잭션으로 묶는다. 중간에 실패하면 전부 롤백한다.

### 5-0. 백업

```sql
CREATE TABLE backup_20260819_opt_item_before_merge        AS SELECT * FROM opt_item;
CREATE TABLE backup_20260819_opt_policy_item_before_merge AS SELECT * FROM opt_policy_item;
CREATE TABLE backup_20260819_order_item_option_before_merge AS SELECT * FROM order_item_option;
```

마지막 것은 349,154행이라 시간이 걸린다. 여유 있을 때 실행한다.

### 5-1. 매핑 테이블 생성

갱신 대상 테이블을 서브쿼리에서 직접 읽지 못하는 MySQL 제약 때문에, 매핑을 먼저 임시 테이블로 만든다.

```sql
CREATE TABLE tmp_opt_dedupe_map AS
SELECT MIN(o.id) AS keep_id, MAX(o.id) AS drop_id, g.id AS group_id, o.name
FROM opt_item o
JOIN opt_group g ON g.id = o.opt_group_id
GROUP BY g.id, o.name
HAVING COUNT(*) > 1;
-- 42행이어야 한다
```

### 5-2. 충돌 행 처리 (A안 기준)

```sql
DELETE oio FROM order_item_option oio
JOIN tmp_opt_dedupe_map m ON oio.opt_item_id = m.drop_id
JOIN order_item_option ex
  ON ex.order_item_id = oio.order_item_id AND ex.opt_item_id = m.keep_id;
-- 2,312행 예상
```

### 5-3. 주문 이력 이관

```sql
UPDATE order_item_option oio
JOIN tmp_opt_dedupe_map m ON oio.opt_item_id = m.drop_id
SET oio.opt_item_id = m.keep_id;
-- 103,331 - 2,312 = 101,019행 예상
```

### 5-4. 정책 연결 정리

```sql
DELETE pi FROM opt_policy_item pi
JOIN tmp_opt_dedupe_map m ON pi.opt_item_id = m.drop_id;
-- 108행 예상
```

### 5-5. 여분 옵션 제거

```sql
DELETE o FROM opt_item o
JOIN tmp_opt_dedupe_map m ON o.id = m.drop_id;
-- 42행 예상 (157 → 115)

DROP TABLE tmp_opt_dedupe_map;
```

## 6. 검증

```sql
-- (1) 그룹 안에 같은 이름이 남지 않아야 한다 → 0행
SELECT g.name, o.name, COUNT(*) c FROM opt_item o
JOIN opt_group g ON g.id = o.opt_group_id
GROUP BY g.id, o.name HAVING c > 1;

-- (2) 메뉴·정책별 옵션명 중복 → 0행
SELECT mop.menu_id, pi.policy_id, oi.name, COUNT(*) c
FROM menu_opt_policy mop
JOIN opt_policy_item pi ON pi.policy_id = mop.policy_id
JOIN opt_item oi ON oi.id = pi.opt_item_id
GROUP BY mop.menu_id, pi.policy_id, oi.name HAVING c > 1;

-- (3) 주문 이력 총량: 작업 전 349,154 → 후 346,842 (충돌 2,312 삭제분)
SELECT COUNT(*) FROM order_item_option;

-- (4) 끊어진 참조가 없어야 한다 → 0행
SELECT COUNT(*) FROM order_item_option oio
LEFT JOIN opt_item o ON o.id = oio.opt_item_id WHERE o.id IS NULL;

-- (5) 옵션이 하나도 없는 정책이 생기면 안 된다 → 0행
SELECT p.id, p.name, COUNT(pi.id) n FROM opt_policy p
LEFT JOIN opt_policy_item pi ON pi.policy_id = p.id
GROUP BY p.id, p.name HAVING n = 0;

-- (6) 주문 금액이 변하지 않았는지 (작업 전후 동일해야 함)
SELECT SUM(total_price) FROM orders;
```

화면 확인: 메뉴 상세의 토핑·세트 사이드·세트 음료·베이스 변경 목록에 중복이 없는지 본다.

## 7. 되돌리기

```sql
-- 순서 주의: 참조 무결성 때문에 opt_item 먼저 복원
INSERT INTO opt_item SELECT * FROM backup_20260819_opt_item_before_merge
  WHERE id NOT IN (SELECT id FROM opt_item);
DELETE FROM opt_policy_item;
INSERT INTO opt_policy_item SELECT * FROM backup_20260819_opt_policy_item_before_merge;
DELETE FROM order_item_option;
INSERT INTO order_item_option SELECT * FROM backup_20260819_order_item_option_before_merge;
```

`order_item_option` 복원은 34만 행이라 시간이 걸린다. 서비스 중단 시간을 고려해 **트래픽이 적은
시간대에 작업**하는 것이 좋다.

## 8. 함께 고칠 것 (선택)

통합하면 `opt_item` 자체가 사라지므로 화면 중복은 해결된다. 다만 다음 두 가지는 별개로 남는다.

1. **조회 쿼리에 `active` 필터가 없다.** `UserMenuMapper.selectOptionItems` 가 `opi.active` 를 보지
   않는다. 지금은 734행 전부 `active=1` 이라 문제가 드러나지 않지만, 앞으로 옵션을 임시로 내릴 때
   동작하지 않는다. `and opi.active = 1` 을 추가해 두면 좋다.
2. **`menu_opt_override` 가 조회에 반영되지 않는다.** 고객 쿼리가 `opi.recommended`,
   `opi.is_default` 를 직접 읽고 override 를 `COALESCE` 하지 않는다. 현재 override 가 0행이라
   드러나지 않지만, 메뉴별 추천 드레싱(FWD-MENU-015)을 쓰기 시작하면 반영되지 않는다.
   `vw_menu_opt_resolved` 뷰가 이미 `COALESCE` 를 하므로 그 뷰로 바꾸는 것이 방법이다.

## 9. 재발 방지

왜 두 벌이 생겼는지는 아직 모른다. `created_at` 이 2026-07-03 으로 동일하고 주문이 반반으로 갈린
것을 보면 **시드 생성 단계에서 두 벌이 만들어진 것**으로 보인다.
`asak-data/seed-v3/opt_item.json` 과 생성 스크립트를 확인해야 같은 일이 반복되지 않는다.

근본적으로는 `opt_item` 에 `UNIQUE(opt_group_id, name)` 제약이 없어서 중복이 들어갈 수 있었다.
통합 후 이 제약을 추가하면 재발을 DB 차원에서 막는다.

```sql
ALTER TABLE opt_item ADD UNIQUE KEY uq_opt_item_group_name (opt_group_id, name);
```

## 10. 대상 42쌍

| 그룹 | 옵션명 | 남길 id | 제거할 id | 주문(남길/제거) |
|---|---|---|---|---|
| 베이스 변경 | 메밀면볼 | 335 | 338 | 681 / 638 |
| 베이스 변경 | 파스타볼 | 336 | 339 | 660 / 668 |
| 베이스 변경 | 포케볼 | 334 | 337 | 652 / 720 |
| 세트 사이드 | 단호박크림스프 | 341 | 346 | 5098 / 5071 |
| 세트 사이드 | 양송이크림스프 | 340 | 345 | 5104 / 4972 |
| 세트 사이드 | 치킨토마토스튜 | 343 | 348 | 5045 / 5052 |
| 세트 사이드 | 카사바칩 | 344 | 349 | 20132 / 4978 |
| 세트 사이드 | 포테이토크림스프 | 342 | 347 | 5010 / 5205 |
| 세트 음료 | 그린밀싹 | 351 | 358 | 3836 / 3808 |
| 세트 음료 | 레드클렌즈 | 353 | 360 | 3891 / 3809 |
| 세트 음료 | 스프라이트제로 | 355 | 362 | 3809 / 3961 |
| 세트 음료 | 아메리카노 | 350 | 357 | 3953 / 3832 |
| 세트 음료 | 애사비사과스파클링 | 356 | 363 | 3950 / 3787 |
| 세트 음료 | 오렌지당근 | 352 | 359 | 3845 / 3922 |
| 세트 음료 | 코크제로 | 354 | 361 | 15399 / 3915 |
| 토핑 추가 | 견과류 | 320 | 327 | 1871 / 1869 |
| 토핑 추가 | 그라브락스연어 | 278 | 290 | 1788 / 1776 |
| 토핑 추가 | 그라운드비프 | 280 | 292 | 1889 / 1830 |
| 토핑 추가 | 김자반 | 325 | 333 | 1799 / 1787 |
| 토핑 추가 | 나쵸칩 | 321 | 328 | 1841 / 1815 |
| 토핑 추가 | 닭가슴살 | 277 | 289 | 1836 / 1811 |
| 토핑 추가 | 당근라페 | 307 | 316 | 1796 / 1760 |
| 토핑 추가 | 두부 | 285 | 300 | 1746 / 1774 |
| 토핑 추가 | 로스트닭다리살 | 279 | 291 | 1742 / 1734 |
| 토핑 추가 | 로스트삼겹 | 283 | 295 | 1881 / 1799 |
| 토핑 추가 | 머쉬룸 | 303 | 311 | 1756 / 1792 |
| 토핑 추가 | 베이컨 | 282 | 294 | 1874 / 1823 |
| 토핑 추가 | 슈레드치즈 | 324 | 331 | 1735 / 1770 |
| 토핑 추가 | 스크램블에그 | 288 | 299 | 1754 / 1857 |
| 토핑 추가 | 양파 | 305 | 313 | 1835 / 1836 |
| 토핑 추가 | 양파플레이크 | 322 | 329 | 1870 / 1794 |
| 토핑 추가 | 에그 | 284 | 296 | 1823 / 1852 |
| 토핑 추가 | 옥수수 | 304 | 312 | 1841 / 1820 |
| 토핑 추가 | 올리브 | 301 | 309 | 1798 / 1756 |
| 토핑 추가 | 우삼겹 | 281 | 293 | 1865 / 1841 |
| 토핑 추가 | 잠봉슬라이스 | 286 | 297 | 1817 / 1891 |
| 토핑 추가 | 적채 | 308 | 317 | 1778 / 1867 |
| 토핑 추가 | 케이준쉬림프 | 287 | 298 | 1810 / 1854 |
| 토핑 추가 | 크랜베리 | 323 | 330 | 1806 / 1853 |
| 토핑 추가 | 토마토 | 306 | 314 | 1831 / 1840 |
| 토핑 추가 | 할라피뇨 | 302 | 310 | 1812 / 1754 |
| 토핑 추가 | 후리가케 | 319 | 326 | 1805 / 1838 |
