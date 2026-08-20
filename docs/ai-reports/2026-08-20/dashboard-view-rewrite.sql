-- =============================================
-- vw_order_list_summary 재작성 — 관리자 대시보드 응답속도 개선
-- 작성: 2026-08-20
-- 실행 대상: asak_db (운영 공용 DB, nam3324.synology.me:33338)
-- 실행자: 사용자 직접 실행 (하네스 auto mode 분류기가 이 세션의 DB 쓰기를 막아서 대행 불가)
-- =============================================
--
-- 배경
--  관리자 대시보드(getDashboard()) 응답이 8초 → DB 호출 횟수 축소로 3.7~3.9초까지 줄었으나,
--  이후 각 Mapper 호출을 프로파일링한 결과 recentOrders(vw_order_list_summary 사용)가
--  약 1.3초로 가장 느렸다. 원인은 뷰 정의 자체가 "LIMIT 3"과 무관하게 order_item
--  전체(8만 3천여 행)를 GROUP_CONCAT으로 먼저 집계한 뒤 정렬·3건만 잘라내는 구조이기
--  때문이다(GROUP BY가 있는 뷰는 MySQL이 병합(merge)하지 못하고 항상 먼저 통째로
--  구체화(materialize)한다).
--
-- 변경 방법
--  파생 테이블 JOIN 대신 주문별 상관 서브쿼리로 바꾼다. 뷰에 최상위 GROUP BY가 없어지면
--  뷰가 "병합 가능"해져서, 이 뷰를 쓰는 쪽(SELECT ... FROM vw_order_list_summary
--  ORDER BY created_at DESC LIMIT 3)의 ORDER BY·LIMIT이 뷰 안으로 밀려 들어간다.
--  그러면 정렬·3건 추리기를 먼저 하고, 그 3건에 대해서만 상관 서브쿼리가 돈다.
--
-- 검증 (2026-08-20, 실제 DB 대상)
--  * 최신 주문 800건 기준 신규 정의와 기존 정의의 모든 컬럼 값이 완전히 일치함(불일치 0건).
--  * `ORDER BY created_at DESC LIMIT 3` 기준 응답시간: 기존 1,288~1,406ms → 신규 92~95ms
--    (약 13배).
--  * `AdminOrderMapper.getOrderList`/`countOrderList`(TODO-007, 아직 미구현·미배선 상태)도
--    같은 뷰를 쓰지만 컬럼 의미는 동일하게 유지되므로 영향 없음.
--
-- 되돌리기
--  문제가 생기면 아래 "OLD DEFINITION" 섹션의 CREATE OR REPLACE VIEW를 그대로 실행하면
--  원상복구된다. (SHOW CREATE VIEW vw_order_list_summary 로 2026-08-20 실행 직전 실측한 것.)
--
-- 실행 후 할 일
--  docs/view.sql 의 vw_order_list_summary 섹션을 이 파일의 NEW DEFINITION 으로 갱신하고,
--  상단 변경 로그에 이번 건을 추가할 것 (Claude에게 "뷰 반영했어, view.sql도 갱신해줘"라고
--  하면 대신 처리 가능).
-- =============================================

-- =============================================
-- NEW DEFINITION — 이것을 실행한다
-- =============================================
CREATE OR REPLACE VIEW `vw_order_list_summary` AS
SELECT
    o.id AS order_id,
    o.order_no AS order_no,
    o.created_at AS created_at,
    o.total_price AS total_price,
    ot.code AS order_type_code,
    ot.name AS order_type_name,
    st.code AS status_code,
    st.name AS status_name,
    ps.code AS payment_status_code,
    ps.name AS payment_status_name,
    (
        SELECT COUNT(*)
        FROM order_item oi
        WHERE
            oi.order_id = o.id
    ) AS line_count,
    (
        SELECT COALESCE(SUM(oi2.quantity), 0)
        FROM order_item oi2
        WHERE
            oi2.order_id = o.id
    ) AS item_count,
    CASE
        WHEN (
            SELECT COUNT(*)
            FROM order_item oi3
            WHERE
                oi3.order_id = o.id
        ) > 1 THEN CONCAT(
            (
                SELECT m.name
                FROM order_item oi4
                    JOIN menu m ON m.id = oi4.menu_id
                WHERE
                    oi4.order_id = o.id
                ORDER BY oi4.id
                LIMIT 1
            ),
            ' 외 ',
            (
                SELECT COUNT(*)
                FROM order_item oi5
                WHERE
                    oi5.order_id = o.id
            ) - 1
        )
        ELSE (
            SELECT m.name
            FROM order_item oi6
                JOIN menu m ON m.id = oi6.menu_id
            WHERE
                oi6.order_id = o.id
            ORDER BY oi6.id
            LIMIT 1
        )
    END AS menu_summary
FROM
    orders o
    JOIN common_code ot ON ot.id = o.order_type_id
    JOIN common_code st ON st.id = o.status_id
    LEFT JOIN payment p ON p.order_id = o.id
    LEFT JOIN common_code ps ON ps.id = p.status_id;

-- =============================================
-- OLD DEFINITION — 되돌릴 때만 실행 (2026-08-20 실행 직전 SHOW CREATE VIEW 실측)
-- =============================================
CREATE ALGORITHM = UNDEFINED DEFINER = `asakasak` @`%` SQL SECURITY DEFINER VIEW `vw_order_list_summary` AS
select
    `o`.`id` AS `order_id`,
    `o`.`order_no` AS `order_no`,
    `o`.`created_at` AS `created_at`,
    `o`.`total_price` AS `total_price`,
    `ot`.`code` AS `order_type_code`,
    `ot`.`name` AS `order_type_name`,
    `st`.`code` AS `status_code`,
    `st`.`name` AS `status_name`,
    `ps`.`code` AS `payment_status_code`,
    `ps`.`name` AS `payment_status_name`,
    `li`.`line_count` AS `line_count`,
    `li`.`item_count` AS `item_count`,
    (
        case
            when (`li`.`line_count` > 1) then concat(
                `li`.`first_menu_name`,
                ' 외 ',
                (`li`.`line_count` - 1)
            )
            else `li`.`first_menu_name`
        end
    ) AS `menu_summary`
from (
        (
            (
                (
                    (
                        `orders` `o`
                        join `common_code` `ot` on (
                            (
                                `ot`.`id` = `o`.`order_type_id`
                            )
                        )
                    )
                    join `common_code` `st` on ((`st`.`id` = `o`.`status_id`))
                )
                left join `payment` `p` on ((`p`.`order_id` = `o`.`id`))
            )
            left join `common_code` `ps` on ((`ps`.`id` = `p`.`status_id`))
        )
        join (
            select
                `x`.`order_id` AS `order_id`, count(0) AS `line_count`, sum(`x`.`quantity`) AS `item_count`, substring_index(
                    group_concat(
                        `x`.`menu_name`
                        order by `x`.`item_id` ASC separator '||'
                    ), '||', 1
                ) AS `first_menu_name`
            from (
                    select
                        `oi`.`id` AS `item_id`, `oi`.`order_id` AS `order_id`, `oi`.`quantity` AS `quantity`, `m`.`name` AS `menu_name`
                    from (
                            `order_item` `oi`
                            join `menu` `m` on ((`m`.`id` = `oi`.`menu_id`))
                        )
                ) `x`
            group by
                `x`.`order_id`
        ) `li` on ((`li`.`order_id` = `o`.`id`))
    );