-- vw_soldout_catalog INGREDIENT 범위 확대 (2026-09-02)
-- 기존: CORE/BASE/제거불가 DEFAULT만 (~20건)
-- 변경: 활성 메뉴에 menu_ing 또는 opt_item(ing_id)으로 연결된 재료 전체 (~80건)
-- TOPPING(59) 포함 · 드레싱·토핑 등 DEFAULT(빼기 가능) 재료도 재료 탭에서 품절 관리

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
  AND (
    EXISTS (
      SELECT 1
      FROM `menu_ing` `mi`
      JOIN `menu` `m`
          ON `m`.`id` = `mi`.`menu_id`
         AND `m`.`deleted_at` IS NULL
      WHERE `mi`.`ing_id` = `i`.`id`
    )
    OR EXISTS (
      SELECT 1
      FROM `opt_item` `oi2`
      JOIN `opt_policy_item` `opi` ON `opi`.`opt_item_id` = `oi2`.`id`
      JOIN `menu_opt_policy` `mop` ON `mop`.`policy_id` = `opi`.`policy_id`
      JOIN `menu` `m` ON `m`.`id` = `mop`.`menu_id` AND `m`.`deleted_at` IS NULL
      WHERE `oi2`.`ing_id` = `i`.`id`
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
  AND (
    `oi`.`ing_id` IS NULL
    OR NOT EXISTS (
      SELECT 1
      FROM `ing` `i2`
      WHERE `i2`.`id` = `oi`.`ing_id`
        AND `i2`.`name` NOT LIKE '%미포함%'
        AND `i2`.`name` NOT REGEXP '[0-9]+배'
        AND `i2`.`name` NOT REGEXP '[xX×][0-9]+'
        AND (
          EXISTS (
            SELECT 1
            FROM `menu_ing` `mi`
            JOIN `menu` `m2`
                ON `m2`.`id` = `mi`.`menu_id`
               AND `m2`.`deleted_at` IS NULL
            WHERE `mi`.`ing_id` = `i2`.`id`
          )
          OR EXISTS (
            SELECT 1
            FROM `opt_item` `oi4`
            JOIN `opt_policy_item` `opi4` ON `opi4`.`opt_item_id` = `oi4`.`id`
            JOIN `menu_opt_policy` `mop4` ON `mop4`.`policy_id` = `opi4`.`policy_id`
            JOIN `menu` `m4` ON `m4`.`id` = `mop4`.`menu_id` AND `m4`.`deleted_at` IS NULL
            WHERE `oi4`.`ing_id` = `i2`.`id`
          )
        )
    )
  );
