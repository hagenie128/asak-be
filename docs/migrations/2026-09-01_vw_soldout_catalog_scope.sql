-- vw_soldout_catalog 범위 정리 (2026-09-01)
-- 1) MENU: soft-delete 제외
-- 2) INGREDIENT: 품절 영향 역할(CORE/BASE/제거불가 DEFAULT) + 이름 노이즈 제외
-- 3) OPTION_ITEM: REQUEST(group_type_id=25) 제외, 활성 메뉴 정책 연결,
--    INGREDIENT에 이미 노출되는 동일 ing_id 중복 제외
-- 4) image_url: photo → icon / icon_url fallback

CREATE OR REPLACE VIEW `vw_soldout_catalog` AS
SELECT
    'MENU' AS `target_type`,
    `m`.`id` AS `target_id`,
    `m`.`name` AS `name`,
    `c`.`name` AS `category`,
    `m`.`sold_out` AS `is_sold_out`,
    `m`.`price` AS `price`,
    `ma`.`url` AS `image_url`
FROM `menu` `m`
JOIN `category` `c` ON `c`.`id` = `m`.`cat_id`
LEFT JOIN `media_asset` `ma`
    ON `ma`.`id` = `m`.`image_asset_id`
   AND `ma`.`deleted_at` IS NULL
WHERE `m`.`deleted_at` IS NULL

UNION ALL

SELECT
    'INGREDIENT' AS `target_type`,
    `i`.`id` AS `target_id`,
    `i`.`name` AS `name`,
    `rt`.`name` AS `category`,
    `i`.`sold_out` AS `is_sold_out`,
    NULL AS `price`,
    COALESCE(`ia_photo`.`url`, `ia_icon`.`url`) AS `image_url`
FROM `ing` `i`
JOIN `common_code` `rt` ON `rt`.`id` = `i`.`type_id`
LEFT JOIN `media_asset` `ia_photo`
    ON `ia_photo`.`id` = `i`.`photo_asset_id`
   AND `ia_photo`.`deleted_at` IS NULL
LEFT JOIN `media_asset` `ia_icon`
    ON `ia_icon`.`id` = `i`.`icon_asset_id`
   AND `ia_icon`.`deleted_at` IS NULL
WHERE `i`.`name` NOT LIKE '%미포함%'
  AND `i`.`name` NOT REGEXP '[0-9]+배'
  AND `i`.`name` NOT REGEXP '[xX×][0-9]+'
  AND EXISTS (
    SELECT 1
    FROM `menu_ing` `mi`
    JOIN `menu` `m`
        ON `m`.`id` = `mi`.`menu_id`
       AND `m`.`deleted_at` IS NULL
    JOIN `common_code` `rc`
        ON `rc`.`id` = `mi`.`role_id`
    WHERE `mi`.`ing_id` = `i`.`id`
      AND (
           `rc`.`code` = 'CORE'
        OR (`rc`.`code` = 'DEFAULT' AND `mi`.`can_remove` = 0)
        OR `rc`.`code` = 'BASE'
      )
  )

UNION ALL

SELECT
    'OPTION_ITEM' AS `target_type`,
    `oi`.`id` AS `target_id`,
    `oi`.`name` AS `name`,
    `og`.`name` AS `category`,
    `oi`.`sold_out` AS `is_sold_out`,
    `oi`.`add_price` AS `price`,
    COALESCE(`oi`.`icon_url`, `ia_photo`.`url`, `ia_icon`.`url`) AS `image_url`
FROM `opt_item` `oi`
JOIN `opt_group` `og` ON `og`.`id` = `oi`.`opt_group_id`
LEFT JOIN `ing` `i` ON `i`.`id` = `oi`.`ing_id`
LEFT JOIN `media_asset` `ia_photo`
    ON `ia_photo`.`id` = `i`.`photo_asset_id`
   AND `ia_photo`.`deleted_at` IS NULL
LEFT JOIN `media_asset` `ia_icon`
    ON `ia_icon`.`id` = `i`.`icon_asset_id`
   AND `ia_icon`.`deleted_at` IS NULL
WHERE `og`.`group_type_id` <> 25
  AND EXISTS (
    SELECT 1
    FROM `opt_policy_item` `opi`
    JOIN `menu_opt_policy` `mop`
        ON `mop`.`policy_id` = `opi`.`policy_id`
    JOIN `menu` `m`
        ON `m`.`id` = `mop`.`menu_id`
       AND `m`.`deleted_at` IS NULL
    WHERE `opi`.`opt_item_id` = `oi`.`id`
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `menu_ing` `mi`
    JOIN `menu` `m2`
        ON `m2`.`id` = `mi`.`menu_id`
       AND `m2`.`deleted_at` IS NULL
    JOIN `common_code` `rc`
        ON `rc`.`id` = `mi`.`role_id`
    WHERE `mi`.`ing_id` = `oi`.`ing_id`
      AND (
           `rc`.`code` = 'CORE'
        OR `rc`.`code` = 'BASE'
        OR (`rc`.`code` = 'DEFAULT' AND `mi`.`can_remove` = 0)
      )
  );
