-- =============================================
-- Project: 아삭 — runtime DB views (MySQL 8)
-- Source: 운영 DB(asak_db) SHOW CREATE VIEW 실측
-- Synced: 2026-08-11
-- Verified: 2026-08-19 (뷰 22개, 실제 DB와 의미 동일 확인)
-- =============================================
--
-- 검증 방법과 결과 (2026-08-19)
--  * 22개 전부 실제 DB에 존재하고, 문서에만 있거나 빠진 뷰는 없다.
--  * 정의를 문자열 리터럴 보존 + 키워드 소문자화 + 공백 정규화 후 비교한 결과
--    20개는 토큰 단위까지 완전 일치했다.
--  * `vw_menu_list`, `vw_menu_opt_policy_json` 두 개는 MySQL이 붙이는 조인 트리
--    괄호가 이 파일에서 생략돼 있다. 가독성을 위해 뺀 것이고 의미는 같다.
--    실제 뷰와 이 파일의 SQL을 각각 실행해 행 수·전체 체크섬·컬럼 순서가
--    모두 같음을 확인했다 (vw_menu_list 72행, vw_menu_opt_policy_json 324행).
--  * 22개 모두 정상 조회되며, backup_* 테이블을 참조하는 뷰는 없다.
--
-- 주의
--  * 이 파일은 가독성을 위해 줄바꿈만 넣은 실측본이다. 뷰를 바꿀 일이 있으면
--    운영 DB에 반영한 뒤 다시 덤프해 갱신한다.
--  * 실제 뷰는 전부 DEFINER=`asakasak`@`%`, SQL SECURITY DEFINER 로 만들어져 있다.
--    이 파일의 CREATE OR REPLACE 문에는 DEFINER 절이 없으므로, 그대로 적용하면
--    실행한 계정이 definer 가 된다. 운영에 적용할 때는 권한 계정으로 실행하거나
--    DEFINER 절을 명시할 것.
--  * 테이블 DDL 은 docs/아삭_mysql.sql 에 있다.
-- =============================================


-- -----------------------------------------------------------------------------
-- vw_menu_availability
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_availability` AS
SELECT `m`.`id` AS `menu_id`,`m`.`sold_out` AS `direct_sold_out`,
       EXISTS(SELECT 1
   FROM ((`menu_ing` `mi`
          JOIN `ing` `i` on((`i`.`id` = `mi`.`ing_id`)))
         JOIN `common_code` `rc` on((`rc`.`id` = `mi`.`role_id`)))
   WHERE ((`mi`.`menu_id` = `m`.`id`)
          AND (`rc`.`code` = 'CORE')
          AND (`i`.`sold_out` = 1))) AS `has_core_sold_out`,
       EXISTS(SELECT `mi`.`menu_id`
   FROM ((`menu_ing` `mi`
          JOIN `ing` `i` on((`i`.`id` = `mi`.`ing_id`)))
         JOIN `common_code` `rc` on((`rc`.`id` = `mi`.`role_id`)))
   WHERE ((`mi`.`menu_id` = `m`.`id`)
          AND (`rc`.`code` = 'BASE'))
   GROUP BY `mi`.`menu_id`
   HAVING (sum((CASE WHEN (`i`.`sold_out` = 0) THEN 1 ELSE 0
                END)) = 0)) AS `base_ing_exhausted`,
       EXISTS(SELECT `op`.`id`
   FROM (((((`menu_opt_policy` `mop`
             JOIN `opt_policy` `op` on((`op`.`id` = `mop`.`policy_id`)))
            JOIN `opt_group` `og` on((`og`.`id` = `op`.`opt_group_id`)))
           JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
          JOIN `opt_policy_item` `opi` on((`opi`.`policy_id` = `op`.`id`)))
         JOIN `opt_item` `oi` on((`oi`.`id` = `opi`.`opt_item_id`)))
   WHERE ((`mop`.`menu_id` = `m`.`id`)
          AND (`gt`.`code` = 'BASE'))
   GROUP BY `op`.`id`
   HAVING (sum((CASE WHEN (`oi`.`sold_out` = 0) THEN 1 ELSE 0
                END)) = 0)) AS `base_opt_exhausted`,
       EXISTS(SELECT 1
   FROM ((`menu_ing` `mi`
          JOIN `ing` `i` on((`i`.`id` = `mi`.`ing_id`)))
         JOIN `common_code` `rc` on((`rc`.`id` = `mi`.`role_id`)))
   WHERE ((`mi`.`menu_id` = `m`.`id`)
          AND (`rc`.`code` = 'DEFAULT')
          AND (`i`.`sold_out` = 1)
          AND (`mi`.`can_remove` = 0))) AS `has_blocking_standard`,
       EXISTS(SELECT `mop`.`policy_id`
   FROM (`menu_opt_policy` `mop`
         JOIN `opt_policy` `op` on((`op`.`id` = `mop`.`policy_id`)))
   WHERE ((`mop`.`menu_id` = `m`.`id`)
          AND ((`mop`.`required` = 1)
               OR (`op`.`required` = 1))
          AND (
                 (SELECT count(0)
                  FROM ((`opt_policy_item` `opi`
                         JOIN `opt_item` `oi` on((`oi`.`id` = `opi`.`opt_item_id`)))
                        LEFT JOIN `menu_opt_override` `mo` on(((`mo`.`menu_id` = `m`.`id`)
                                                               AND (`mo`.`opt_item_id` = `opi`.`opt_item_id`))))
                  WHERE ((`opi`.`policy_id` = `op`.`id`)
                         AND (coalesce(`mo`.`active`, `opi`.`active`) = 1)
                         AND (`oi`.`sold_out` = 0))) < `op`.`min_select`))) AS `has_exhausted_required_group`
FROM `menu` `m`;

-- -----------------------------------------------------------------------------
-- vw_menu_ing_detail
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_ing_detail` AS
SELECT `mi`.`menu_id` AS `menu_id`,`i`.`id` AS `ing_id`,`i`.`name` AS `ing_name`,`i`.`sold_out` AS `ing_sold_out`,
       `mi`.`role_id` AS `role_id`,`mi`.`quantity` AS `quantity`,`mi`.`unit_id` AS `unit_id`,`mi`.`is_default` AS `is_default`,
       `mi`.`can_remove` AS `can_remove`,`mi`.`sort_no` AS `sort_no`,`a`.`id` AS `allergen_id`,`a`.`name` AS `allergen_name`
FROM (((`menu_ing` `mi`
        JOIN `ing` `i` on((`i`.`id` = `mi`.`ing_id`)))
       LEFT JOIN `ing_allergen` `ia` on((`ia`.`ing_id` = `i`.`id`)))
      LEFT JOIN `allergen` `a` on((`a`.`id` = `ia`.`allergen_id`)))
ORDER BY `mi`.`menu_id`,`mi`.`sort_no`;

-- -----------------------------------------------------------------------------
-- vw_menu_ing_json
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_ing_json` AS
SELECT `mi`.`menu_id` AS `menu_id`,`i`.`id` AS `ingredient_id`,`i`.`name` AS `ing_name`,`i`.`sold_out` AS `ing_sold_out`,
       lower(`rc`.`code`) AS `role`,`mi`.`quantity` AS `quantity`,`ut`.`code` AS `unit`,`mi`.`is_default` AS `is_default`,
       `mi`.`can_remove` AS `can_remove`,`mi`.`sort_no` AS `sort_no`,
       coalesce(
                  (SELECT json_arrayagg(json_object('id',
                                          `a`.`id`, 'name', `a`.`name`))
                   FROM (`ing_allergen` `ia`
                         JOIN `allergen` `a` on((`a`.`id` = `ia`.`allergen_id`)))
                   WHERE (`ia`.`ing_id` = `i`.`id`)),json_array()) AS `allergens`
FROM (((`menu_ing` `mi`
        JOIN `ing` `i` on((`i`.`id` = `mi`.`ing_id`)))
       JOIN `common_code` `rc` on((`rc`.`id` = `mi`.`role_id`)))
      LEFT JOIN `common_code` `ut` on((`ut`.`id` = `mi`.`unit_id`)))
ORDER BY `mi`.`menu_id`,`mi`.`sort_no`;

-- -----------------------------------------------------------------------------
-- vw_menu_list
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_list` AS
SELECT `m`.`id` AS `menu_id`,`m`.`cat_id` AS `category_id`,`m`.`name` AS `name`,`m`.`price` AS `price`,
       `ma`.`url` AS `image_url`,`mn`.`kcal` AS `base_kcal`,`m`.`sold_out` AS `is_sold_out`,
       ((0 <> `va`.`has_core_sold_out`)
        OR (0 <> `va`.`base_ing_exhausted`)
        OR (0 <> `va`.`base_opt_exhausted`)
        OR (0 <> `va`.`has_blocking_standard`)) AS `has_sold_out_ingredient`,
       ((0 = `va`.`direct_sold_out`)
        AND (0 = `va`.`has_core_sold_out`)
        AND (0 = `va`.`base_ing_exhausted`)
        AND (0 = `va`.`base_opt_exhausted`)
        AND (0 = `va`.`has_blocking_standard`)
        AND (0 = `va`.`has_exhausted_required_group`)) AS `is_orderable`
FROM ((`menu` `m`
       LEFT JOIN `menu_nutr` `mn` on((`mn`.`menu_id` = `m`.`id`)))
      JOIN `vw_menu_availability` `va` on((`va`.`menu_id` = `m`.`id`)))
     LEFT JOIN `media_asset` `ma` on(((`ma`.`id` = `m`.`image_asset_id`)
        AND (`ma`.`deleted_at` IS NULL)))
WHERE (`m`.`deleted_at` IS NULL);

-- -----------------------------------------------------------------------------
-- vw_menu_opt_policy_json
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_opt_policy_json` AS
SELECT `x`.`menu_id` AS `menu_id`,`x`.`option_group_id` AS `option_group_id`,`x`.`name` AS `name`,
       `x`.`policy_name` AS `policy_name`,`x`.`group_type` AS `group_type`,`x`.`select_type` AS `select_type`,
       `x`.`min_select` AS `min_select`,`x`.`max_select` AS `max_select`,`x`.`sort_order` AS `sort_order`,
       `x`.`is_required` AS `is_required`,
       json_arrayagg(json_object('optionItemId',
                       `x`.`option_item_id`, 'ingredientId', `x`.`ingredient_id`, 'name', `x`.`item_name`,
                       'extraPrice', `x`.`extra_price`, 'originalPrice', `x`.`original_price`, 'servingAmount',
                       `x`.`serving_amount`, 'servingUnit', `x`.`serving_unit`, 'iconUrl', `x`.`icon_url`, 'colorHex',
                       `x`.`color_hex`, 'isSoldOut', `x`.`is_sold_out`, 'extraKcal', `x`.`extra_kcal`, 'proteinG',
                       `x`.`protein_g`, 'isRecommended', `x`.`is_recommended`, 'isDefault', `x`.`is_default`)) AS `items`
FROM
  (SELECT `mop`.`menu_id` AS `menu_id`,`op`.`opt_group_id` AS `option_group_id`,`og`.`name` AS `name`,`op`.`name` AS `policy_name`,
          `cg_group`.`code` AS `group_type`,(CASE WHEN (`og`.`max_select` = 1) THEN 'SINGLE' ELSE 'MULTI'
                                             END) AS `select_type`,
          `og`.`min_select` AS `min_select`,`og`.`max_select` AS `max_select`,`mop`.`sort_no` AS `sort_order`,
          `mop`.`required` AS `is_required`,`oi`.`id` AS `option_item_id`,`oi`.`ing_id` AS `ingredient_id`,`oi`.`name` AS `item_name`,
          `oi`.`add_price` AS `extra_price`,`oi`.`list_price` AS `original_price`,`oi`.`amount` AS `serving_amount`,
          `cc_unit`.`code` AS `serving_unit`,`oi`.`icon_url` AS `icon_url`,`oi`.`color_hex` AS `color_hex`,
          `oi`.`sold_out` AS `is_sold_out`,`n`.`kcal` AS `extra_kcal`,`n`.`protein_g` AS `protein_g`,
          coalesce(`moo`.`recommended`, `opi`.`recommended`, 0) AS `is_recommended`,
          coalesce(`moo`.`is_default`, `opi`.`is_default`, 0) AS `is_default`,
          coalesce(`moo`.`sort_no`, `opi`.`sort_no`, 9999) AS `item_sort_no`
   FROM `menu_opt_policy` `mop`
   JOIN `opt_policy` `op` ON `op`.`id` = `mop`.`policy_id`
   JOIN `opt_group` `og` ON `og`.`id` = `op`.`opt_group_id`
   LEFT JOIN `common_code` `cg_group` ON `cg_group`.`id` = `og`.`group_type_id`
   JOIN `opt_policy_item` `opi` ON `opi`.`policy_id` = `op`.`id`
   JOIN `opt_item` `oi` ON `oi`.`id` = `opi`.`opt_item_id`
   LEFT JOIN `ing` `i` ON `i`.`id` = `oi`.`ing_id`
   LEFT JOIN `ing_nutr` `n` ON `n`.`ing_id` = `i`.`id`
   LEFT JOIN `menu_opt_override` `moo`
     ON `moo`.`menu_id` = `mop`.`menu_id` AND `moo`.`opt_item_id` = `oi`.`id`
   LEFT JOIN `common_code` `cc_unit` ON `cc_unit`.`id` = `oi`.`unit_id`
   ORDER BY `mop`.`menu_id`,`mop`.`sort_no`,`op`.`opt_group_id`,
            coalesce(`moo`.`sort_no`, `opi`.`sort_no`, 9999), `oi`.`id`) `x`
GROUP BY `x`.`menu_id`,`x`.`option_group_id`,`x`.`name`,`x`.`policy_name`,`x`.`group_type`,`x`.`select_type`,
         `x`.`min_select`,`x`.`max_select`,`x`.`sort_order`,`x`.`is_required`;

-- -----------------------------------------------------------------------------
-- vw_menu_opt_resolved
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_menu_opt_resolved` AS
SELECT `mop`.`menu_id` AS `menu_id`,`op`.`id` AS `policy_id`,`op`.`name` AS `policy_name`,`op`.`min_select` AS `min_select`,
       `op`.`max_select` AS `max_select`,`op`.`required` AS `policy_required`,`oi`.`id` AS `opt_item_id`,
       `oi`.`name` AS `opt_item_name`,`oi`.`add_price` AS `add_price`,`oi`.`sold_out` AS `opt_item_sold_out`,
       coalesce(`mo`.`recommended`, `opi`.`recommended`) AS `recommended`,coalesce(`mo`.`is_default`,
                                                                            `opi`.`is_default`) AS `is_default`,
       coalesce(`mo`.`sort_no`, `opi`.`sort_no`) AS `sort_no`,coalesce(`mo`.`active`,
                                                                `opi`.`active`) AS `active`
FROM ((((`menu_opt_policy` `mop`
         JOIN `opt_policy` `op` on((`op`.`id` = `mop`.`policy_id`)))
        JOIN `opt_policy_item` `opi` on((`opi`.`policy_id` = `op`.`id`)))
       JOIN `opt_item` `oi` on((`oi`.`id` = `opi`.`opt_item_id`)))
      LEFT JOIN `menu_opt_override` `mo` on(((`mo`.`menu_id` = `mop`.`menu_id`)
                                             AND (`mo`.`opt_item_id` = `opi`.`opt_item_id`))))
ORDER BY `mop`.`menu_id`,coalesce(`mo`.`sort_no`, `opi`.`sort_no`);

-- -----------------------------------------------------------------------------
-- vw_order_item_base_dressing
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_base_dressing` AS
SELECT `oio`.`order_item_id` AS `order_item_id`,max((CASE
                                                         WHEN (`gt`.`code` = 'BASE') THEN `oit`.`name`
                                                     END)) AS `base_name`,
       max((CASE WHEN (`gt`.`code` = 'DRESSING') THEN `oit`.`name`
            END)) AS `dressing_name`
FROM (((`order_item_option` `oio`
        JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
       JOIN `opt_group` `og` on((`og`.`id` = `oit`.`opt_group_id`)))
      JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
WHERE (`gt`.`code` IN ('BASE','DRESSING'))
GROUP BY `oio`.`order_item_id`;

-- -----------------------------------------------------------------------------
-- vw_order_item_detail
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_detail` AS
SELECT `o`.`id` AS `order_id`,`o`.`order_no` AS `order_no`,`oi`.`id` AS `order_item_id`,`oi`.`menu_id` AS `menu_id`,
       `m`.`name` AS `menu_name`,`oi`.`quantity` AS `quantity`,`oi`.`price` AS `price`
FROM ((`orders` `o`
       JOIN `order_item` `oi` on((`oi`.`order_id` = `o`.`id`)))
      JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))
ORDER BY `o`.`id`,`oi`.`id`;

-- -----------------------------------------------------------------------------
-- vw_order_item_exclusion
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_exclusion` AS
SELECT `ie`.`order_item_id` AS `order_item_id`,`ie`.`ing_id` AS `ing_id`,`i`.`name` AS `ing_name`
FROM (`item_exclusion` `ie`
      JOIN `ing` `i` on((`i`.`id` = `ie`.`ing_id`)))
ORDER BY `ie`.`order_item_id`,`ie`.`ing_id`;

-- -----------------------------------------------------------------------------
-- vw_order_item_full
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_full` AS
SELECT `oi`.`order_id` AS `order_id`,`oi`.`menu_id` AS `menu_id`,`m`.`name` AS `menu_name`,`oi`.`quantity` AS `quantity`,
       `oi`.`price` AS `unit_price`,

  (SELECT json_arrayagg(json_object('optionItemId',
                          `oio`.`opt_item_id`, 'name', `oit`.`name`, 'quantity', `oio`.`quantity`, 'price',
                          `oio`.`price`))
   FROM (((`order_item_option` `oio`
           JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
          JOIN `opt_group` `og` on((`og`.`id` = `oit`.`opt_group_id`)))
         JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
   WHERE ((`oio`.`order_item_id` = `oi`.`id`)
          AND (`gt`.`code` <> 'REQUEST'))) AS `option_items`,

  (SELECT json_arrayagg(json_object('ingredientId', `ie`.`ing_id`, 'name', `i`.`name`))
   FROM (`item_exclusion` `ie`
         JOIN `ing` `i` on((`i`.`id` = `ie`.`ing_id`)))
   WHERE (`ie`.`order_item_id` = `oi`.`id`)) AS `excluded_ingredients`
FROM (`order_item` `oi`
      JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))
ORDER BY `oi`.`order_id`,`oi`.`id`;

-- -----------------------------------------------------------------------------
-- vw_order_item_option
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_option` AS
SELECT `oio`.`order_item_id` AS `order_item_id`,`oio`.`opt_item_id` AS `opt_item_id`,`oit`.`name` AS `opt_item_name`,
       `oio`.`quantity` AS `quantity`,`oio`.`price` AS `price`,`oit`.`opt_group_id` AS `opt_gruop`
FROM (`order_item_option` `oio`
      JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
ORDER BY `oio`.`order_item_id`,`oio`.`opt_item_id`;

-- -----------------------------------------------------------------------------
-- vw_order_item_tag
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_item_tag` AS
SELECT `oio`.`order_item_id` AS `order_item_id`,
       (CASE `gt`.`code` WHEN 'SET_SIDE' THEN 'side' WHEN 'SET_DRINK' THEN 'drink' ELSE 'plus'
        END) AS `tone`,`oit`.`name` AS `label`
FROM (((`order_item_option` `oio`
        JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
       JOIN `opt_group` `og` on((`og`.`id` = `oit`.`opt_group_id`)))
      JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
WHERE (`gt`.`code` NOT IN ('BASE','DRESSING','REQUEST'))
UNION ALL
SELECT `ie`.`order_item_id` AS `order_item_id`,'exclude' AS `tone`,`i`.`name` AS `label`
FROM (`item_exclusion` `ie`
      JOIN `ing` `i` on((`i`.`id` = `ie`.`ing_id`)));

-- -----------------------------------------------------------------------------
-- vw_order_list_summary
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_list_summary` AS
SELECT `o`.`id` AS `order_id`,`o`.`order_no` AS `order_no`,`o`.`created_at` AS `created_at`,
       `o`.`total_price` AS `total_price`,`ot`.`code` AS `order_type_code`,`ot`.`name` AS `order_type_name`,
       `st`.`code` AS `status_code`,`st`.`name` AS `status_name`,`ps`.`code` AS `payment_status_code`,
       `ps`.`name` AS `payment_status_name`,`li`.`line_count` AS `line_count`,`li`.`item_count` AS `item_count`,
       (CASE
            WHEN (`li`.`line_count` > 1) THEN concat(`li`.`first_menu_name`,
                                                ' 외 ',(`li`.`line_count` - 1)) ELSE `li`.`first_menu_name`
        END) AS `menu_summary`
FROM (((((`orders` `o`
          JOIN `common_code` `ot` on((`ot`.`id` = `o`.`order_type_id`)))
         JOIN `common_code` `st` on((`st`.`id` = `o`.`status_id`)))
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      JOIN
        (SELECT `x`.`order_id` AS `order_id`,count(0) AS `line_count`,sum(`x`.`quantity`) AS `item_count`,
                substring_index(group_concat(`x`.`menu_name`
                                             ORDER BY `x`.`item_id` ASC separator '||'), '||', 1) AS `first_menu_name`
         FROM
           (SELECT `oi`.`id` AS `item_id`,`oi`.`order_id` AS `order_id`,`oi`.`quantity` AS `quantity`,
                   `m`.`name` AS `menu_name`
            FROM (`order_item` `oi`
                  JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))) `x`
         GROUP BY `x`.`order_id`) `li` on((`li`.`order_id` = `o`.`id`)));

-- -----------------------------------------------------------------------------
-- vw_order_live
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_live` AS
SELECT `o`.`id` AS `order_id`,`o`.`order_no` AS `order_no`,`ot`.`name` AS `order_type_label`,`st`.`code` AS `status_code`,
       `o`.`total_price` AS `total_price`,`o`.`created_at` AS `created_at`,
       timestampdiff(SECOND, `o`.`created_at`, now()) AS `elapsed_sec`,
       json_arrayagg(json_object('menuId',
                       `oi`.`menu_id`, 'menuName', `m`.`name`, 'quantity', `oi`.`quantity`, 'unitPrice',
                       `oi`.`price`, 'base', `bd`.`base_name`, 'dressing', `bd`.`dressing_name`, 'options',
                       coalesce(`tags`.`options`, json_array()))) AS `menus`
FROM ((((((`orders` `o`
           JOIN `common_code` `ot` on((`ot`.`id` = `o`.`order_type_id`)))
          JOIN `common_code` `st` on((`st`.`id` = `o`.`status_id`)))
         JOIN `order_item` `oi` on((`oi`.`order_id` = `o`.`id`)))
        JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))
       LEFT JOIN
         (SELECT `oio`.`order_item_id` AS `order_item_id`,max((CASE
                                                                   WHEN (`gt`.`code` = 'BASE') THEN `oit`.`name`
                                                               END)) AS `base_name`,
                 max((CASE WHEN (`gt`.`code` = 'DRESSING') THEN `oit`.`name`
                      END)) AS `dressing_name`
          FROM ((((((`orders` `active_o`
                     JOIN `common_code` `active_st` on((`active_st`.`id` = `active_o`.`status_id`)))
                    JOIN `order_item` `active_oi` on((`active_oi`.`order_id` = `active_o`.`id`)))
                   JOIN `order_item_option` `oio` on((`oio`.`order_item_id` = `active_oi`.`id`)))
                  JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
                 JOIN `opt_group` `og` on((`og`.`id` = `oit`.`opt_group_id`)))
                JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
          WHERE ((`active_st`.`code` IN ('RECEIVED','PREPARING'))
                 AND (`gt`.`code` IN ('BASE','DRESSING')))
          GROUP BY `oio`.`order_item_id`) `bd` on((`bd`.`order_item_id` = `oi`.`id`)))
      LEFT JOIN
        (SELECT `tag_rows`.`order_item_id` AS `order_item_id`,
                json_arrayagg(json_object('tone',
                                `tag_rows`.`tone`, 'label', `tag_rows`.`label`)) AS `options`
         FROM
           (SELECT `oio`.`order_item_id` AS `order_item_id`,
                   (CASE WHEN (`gt`.`code` = 'SET_SIDE') THEN 'side' WHEN (`gt`.`code` = 'SET_DRINK') THEN 'drink' ELSE 'plus'
                    END) AS `tone`,`oit`.`name` AS `label`
            FROM ((((((`orders` `active_o`
                       JOIN `common_code` `active_st` on((`active_st`.`id` = `active_o`.`status_id`)))
                      JOIN `order_item` `active_oi` on((`active_oi`.`order_id` = `active_o`.`id`)))
                     JOIN `order_item_option` `oio` on((`oio`.`order_item_id` = `active_oi`.`id`)))
                    JOIN `opt_item` `oit` on((`oit`.`id` = `oio`.`opt_item_id`)))
                   JOIN `opt_group` `og` on((`og`.`id` = `oit`.`opt_group_id`)))
                  JOIN `common_code` `gt` on((`gt`.`id` = `og`.`group_type_id`)))
            WHERE ((`active_st`.`code` IN ('RECEIVED','PREPARING'))
                   AND (`gt`.`code` NOT IN ('BASE','DRESSING','REQUEST')))
            UNION ALL SELECT `ie`.`order_item_id` AS `order_item_id`,'exclude' AS `tone`,`i`.`name` AS `label`
            FROM ((((`orders` `active_o`
                     JOIN `common_code` `active_st` on((`active_st`.`id` = `active_o`.`status_id`)))
                    JOIN `order_item` `active_oi` on((`active_oi`.`order_id` = `active_o`.`id`)))
                   JOIN `item_exclusion` `ie` on((`ie`.`order_item_id` = `active_oi`.`id`)))
                  JOIN `ing` `i` on((`i`.`id` = `ie`.`ing_id`)))
            WHERE (`active_st`.`code` IN ('RECEIVED','PREPARING'))) `tag_rows`
         GROUP BY `tag_rows`.`order_item_id`) `tags` on((`tags`.`order_item_id` = `oi`.`id`)))
WHERE (`st`.`code` IN ('RECEIVED','PREPARING'))
GROUP BY `o`.`id`,`o`.`order_no`,`ot`.`name`,`st`.`code`,`o`.`total_price`,`o`.`created_at`;

-- -----------------------------------------------------------------------------
-- vw_order_status_summary
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_status_summary` AS
SELECT cast(`o`.`created_at` AS date) AS `order_date`,`ot`.`code` AS `order_type_code`,`ot`.`name` AS `order_type_name`,
       `st`.`code` AS `status_code`,`st`.`name` AS `status_name`,count(0) AS `order_count`
FROM ((`orders` `o`
       JOIN `common_code` `ot` on((`ot`.`id` = `o`.`order_type_id`)))
      JOIN `common_code` `st` on((`st`.`id` = `o`.`status_id`)))
GROUP BY `order_date`,`ot`.`code`,`ot`.`name`,`st`.`code`,`st`.`name`;

-- -----------------------------------------------------------------------------
-- vw_order_summary
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_order_summary` AS
SELECT `o`.`id` AS `order_id`,`o`.`order_no` AS `order_no`,`o`.`total_price` AS `total_price`,
       `o`.`created_at` AS `created_at`,`ot`.`code` AS `order_type_code`,`ot`.`name` AS `order_type_name`,
       `st`.`code` AS `status_code`,`st`.`name` AS `status_name`,`p`.`id` AS `payment_id`,`p`.`amount` AS `paid_amount`,
       `p`.`paid_at` AS `paid_at`,`ps`.`code` AS `payment_status_code`,`ps`.`name` AS `payment_status_name`,
       `pm`.`name` AS `payment_method_name`
FROM (((((`orders` `o`
          JOIN `common_code` `ot` on((`ot`.`id` = `o`.`order_type_id`)))
         JOIN `common_code` `st` on((`st`.`id` = `o`.`status_id`)))
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      LEFT JOIN `pay_method_cfg` `pm` on((`pm`.`method_id` = `p`.`method_id`)))
ORDER BY `o`.`created_at` DESC;

-- -----------------------------------------------------------------------------
-- vw_payment_result
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_payment_result` AS
SELECT `p`.`id` AS `payment_id`,`p`.`order_id` AS `order_id`,`o`.`order_no` AS `order_no`,`ps`.`code` AS `payment_status`,
       `p`.`amount` AS `approved_amount`,`p`.`paid_at` AS `approved_at`,

  (SELECT count(0)
   FROM (`orders` `o2`
         JOIN `common_code` `st2` on((`st2`.`id` = `o2`.`status_id`)))
   WHERE (`st2`.`code` IN ('RECEIVED','PREPARING'))) AS `waiting_order_count`
FROM ((`payment` `p`
       JOIN `orders` `o` on((`o`.`id` = `p`.`order_id`)))
      JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)));

-- -----------------------------------------------------------------------------
-- vw_sales_daily
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_sales_daily` AS
SELECT cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date) AS `sales_date`,
       count(DISTINCT (CASE
                           WHEN ((`p`.`paid_at` IS NOT NULL)
                                 AND (`os`.`code` <> 'CANCELED')
                                 AND (`ps`.`code` NOT IN ('CANCELED',
                                                      'REFUNDED'))) THEN `o`.`id`
                       END)) AS `order_count`,
       count(DISTINCT (CASE
                           WHEN ((`os`.`code` = 'CANCELED')
                                 OR (`ps`.`code` IN ('CANCELED',
                                                 'REFUNDED'))) THEN `o`.`id`
                       END)) AS `canceled_order_count`,
       coalesce(sum((CASE WHEN (`p`.`paid_at` IS NOT NULL) THEN `p`.`amount` ELSE 0
                     END)), 0) AS `gross_sales_amount`,
       coalesce(sum((CASE
                         WHEN ((`p`.`paid_at` IS NOT NULL)
                               AND ((`os`.`code` = 'CANCELED')
                                    OR (`ps`.`code` IN ('CANCELED',
                                                      'REFUNDED')))) THEN `p`.`amount` ELSE 0
                     END)), 0) AS `canceled_amount`,
       (coalesce(sum((CASE WHEN (`p`.`paid_at` IS NOT NULL) THEN `p`.`amount` ELSE 0
                      END)), 0) - coalesce(sum((CASE
                                                    WHEN ((`p`.`paid_at` IS NOT NULL)
                                                          AND ((`os`.`code` = 'CANCELED')
                                                               OR (`ps`.`code` IN ('CANCELED',
                                                                                 'REFUNDED')))) THEN `p`.`amount` ELSE 0
                                                END)), 0)) AS `net_sales_amount`
FROM (((`orders` `o`
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      LEFT JOIN `common_code` `os` on((`os`.`id` = `o`.`status_id`)))
GROUP BY cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date);

-- -----------------------------------------------------------------------------
-- vw_sales_hourly
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_sales_hourly` AS
SELECT cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date) AS `sales_date`,hour(coalesce(`p`.`paid_at`,

                                                                                      `o`.`created_at`)) AS `sales_hour`,
       count(DISTINCT (CASE
                           WHEN ((`p`.`paid_at` IS NOT NULL)
                                 AND (`os`.`code` <> 'CANCELED')
                                 AND (`ps`.`code` NOT IN ('CANCELED',
                                                      'REFUNDED'))) THEN `o`.`id`
                       END)) AS `order_count`,
       count(DISTINCT (CASE
                           WHEN ((`os`.`code` = 'CANCELED')
                                 OR (`ps`.`code` IN ('CANCELED',
                                                 'REFUNDED'))) THEN `o`.`id`
                       END)) AS `canceled_order_count`,
       coalesce(sum((CASE WHEN (`p`.`paid_at` IS NOT NULL) THEN `p`.`amount` ELSE 0
                     END)), 0) AS `gross_sales_amount`,
       coalesce(sum((CASE
                         WHEN ((`p`.`paid_at` IS NOT NULL)
                               AND ((`os`.`code` = 'CANCELED')
                                    OR (`ps`.`code` IN ('CANCELED',
                                                      'REFUNDED')))) THEN `p`.`amount` ELSE 0
                     END)), 0) AS `canceled_amount`,
       (coalesce(sum((CASE WHEN (`p`.`paid_at` IS NOT NULL) THEN `p`.`amount` ELSE 0
                      END)), 0) - coalesce(sum((CASE
                                                    WHEN ((`p`.`paid_at` IS NOT NULL)
                                                          AND ((`os`.`code` = 'CANCELED')
                                                               OR (`ps`.`code` IN ('CANCELED',
                                                                                 'REFUNDED')))) THEN `p`.`amount` ELSE 0
                                                END)), 0)) AS `net_sales_amount`
FROM (((`orders` `o`
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      LEFT JOIN `common_code` `os` on((`os`.`id` = `o`.`status_id`)))
GROUP BY cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date),hour(coalesce(`p`.`paid_at`,
                                                                        `o`.`created_at`));

-- -----------------------------------------------------------------------------
-- vw_soldout_catalog
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_soldout_catalog` AS
SELECT 'MENU' AS `target_type`,`m`.`id` AS `target_id`,`m`.`name` AS `name`,`c`.`name` AS `category`,
       `m`.`sold_out` AS `is_sold_out`,`m`.`price` AS `price`
FROM (`menu` `m`
      JOIN `category` `c` on((`c`.`id` = `m`.`cat_id`)))
UNION ALL
SELECT 'INGREDIENT' AS `target_type`,`i`.`id` AS `target_id`,`i`.`name` AS `name`,`rt`.`name` AS `category`,
       `i`.`sold_out` AS `is_sold_out`,NULL AS `price`
FROM (`ing` `i`
      JOIN `common_code` `rt` on((`rt`.`id` = `i`.`type_id`)))
UNION ALL
SELECT 'OPTION_ITEM' AS `target_type`,`oi`.`id` AS `target_id`,`oi`.`name` AS `name`,`og`.`name` AS `category`,
       `oi`.`sold_out` AS `is_sold_out`,`oi`.`add_price` AS `price`
FROM (`opt_item` `oi`
      JOIN `opt_group` `og` on((`og`.`id` = `oi`.`opt_group_id`)));

-- -----------------------------------------------------------------------------
-- vw_top_menu_daily
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_top_menu_daily` AS
SELECT cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date) AS `sales_date`,`m`.`id` AS `menu_id`,`m`.`name` AS `menu_name`,sum(`oi`.`quantity`) AS `quantity`,
       count(DISTINCT `o`.`id`) AS `order_count`,sum((`oi`.`price` * `oi`.`quantity`)) AS `sales_amount`
FROM (((((`orders` `o`
          JOIN `order_item` `oi` on((`oi`.`order_id` = `o`.`id`)))
         JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      LEFT JOIN `common_code` `os` on((`os`.`id` = `o`.`status_id`)))
WHERE ((`p`.`paid_at` IS NOT NULL)
       AND (`os`.`code` <> 'CANCELED')
       AND (`ps`.`code` NOT IN ('CANCELED','REFUNDED')))
GROUP BY cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date),`m`.`id`,`m`.`name`;

-- -----------------------------------------------------------------------------
-- vw_top_menu_hourly
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW `vw_top_menu_hourly` AS
SELECT cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date) AS `sales_date`,hour(coalesce(`p`.`paid_at`,

                                                                                      `o`.`created_at`)) AS `sales_hour`,`m`.`id` AS `menu_id`,
       `m`.`name` AS `menu_name`,sum(`oi`.`quantity`) AS `quantity`,count(DISTINCT `o`.`id`) AS `order_count`,
       sum((`oi`.`price` * `oi`.`quantity`)) AS `sales_amount`
FROM (((((`orders` `o`
          JOIN `order_item` `oi` on((`oi`.`order_id` = `o`.`id`)))
         JOIN `menu` `m` on((`m`.`id` = `oi`.`menu_id`)))
        LEFT JOIN `payment` `p` on((`p`.`order_id` = `o`.`id`)))
       LEFT JOIN `common_code` `ps` on((`ps`.`id` = `p`.`status_id`)))
      LEFT JOIN `common_code` `os` on((`os`.`id` = `o`.`status_id`)))
WHERE ((`p`.`paid_at` IS NOT NULL)
       AND (`os`.`code` <> 'CANCELED')
       AND (`ps`.`code` NOT IN ('CANCELED','REFUNDED')))
GROUP BY cast(coalesce(`p`.`paid_at`, `o`.`created_at`) AS date),hour(coalesce(`p`.`paid_at`,
                                                                        `o`.`created_at`)),`m`.`id`,`m`.`name`;
