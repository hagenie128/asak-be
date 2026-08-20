-- =============================================
-- vw_order_list_summary 원복 — dashboard-view-rewrite.sql 적용 결과가 오히려 5배 느려져서 되돌림
-- 작성: 2026-08-20
-- 실행 대상: asak_db (운영 공용 DB, nam3324.synology.me:33338)
--
-- 무슨 일이 있었나
--  correlated subquery로 재작성한 버전을 실행했더니 SELECT * FROM vw_order_list_summary
--  ORDER BY created_at DESC LIMIT 3 기준 1.3초 -> 6.6~6.8초로 오히려 5배 느려졌다.
--  EXPLAIN으로 확인해보니 뷰가 병합(merge)되지 않고 여전히 TEMPTABLE(Materialize)로
--  전체 51,801건에 대해 상관 서브쿼리 6개를 다 실행한 뒤에야 정렬·LIMIT 3을 적용했다.
--  (MySQL은 SELECT 목록에 상관 서브쿼리가 있으면 뷰를 병합하지 않는다 — 사전에 raw SQL로만
--  속도를 검증하고 실제 CREATE VIEW로는 재검증하지 않아서 이 차이를 놓쳤다.)
--
-- 이 파일은 2026-08-20 dashboard-view-rewrite.sql 실행 직전 SHOW CREATE VIEW로 실측해둔
-- 원래 정의를 그대로 복원한다.
-- =============================================

CREATE OR REPLACE VIEW `vw_order_list_summary` AS
select `o`.`id` AS `order_id`,`o`.`order_no` AS `order_no`,`o`.`created_at` AS `created_at`,
       `o`.`total_price` AS `total_price`,`ot`.`code` AS `order_type_code`,`ot`.`name` AS `order_type_name`,
       `st`.`code` AS `status_code`,`st`.`name` AS `status_name`,`ps`.`code` AS `payment_status_code`,
       `ps`.`name` AS `payment_status_name`,`li`.`line_count` AS `line_count`,`li`.`item_count` AS `item_count`,
       (case when (`li`.`line_count` > 1) then concat(`li`.`first_menu_name`,' 외 ',(`li`.`line_count` - 1))
             else `li`.`first_menu_name` end) AS `menu_summary`
from (((((`orders` `o`
          join `common_code` `ot` on((`ot`.`id` = `o`.`order_type_id`)))
         join `common_code` `st` on((`st`.`id` = `o`.`status_id`)))
        left join `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       left join `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      join
        (select `x`.`order_id` AS `order_id`,count(0) AS `line_count`,sum(`x`.`quantity`) AS `item_count`,
                substring_index(group_concat(`x`.`menu_name` order by `x`.`item_id` ASC separator '||'),'||',1) AS `first_menu_name`
         from (select `oi`.`id` AS `item_id`,`oi`.`order_id` AS `order_id`,`oi`.`quantity` AS `quantity`,
                      `m`.`name` AS `menu_name`
               from (`order_item` `oi`
                     join `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))) `x`
         group by `x`.`order_id`) `li` on((`li`.`order_id` = `o`.`id`)));
