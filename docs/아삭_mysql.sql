-- =============================================
-- Project: 아삭
-- DBMS: MySQL
-- Target/model charset: utf8mb4
-- SQL file encoding: UTF-8
-- Generated: 2026-08-07 08:56:59 UTC
-- =============================================

SET NAMES 'utf8mb4';
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- Tables
-- =============================================

CREATE TABLE `allergen` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `category` (
    `sort_no` INT NOT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `active` TINYINT(1) NOT NULL,
    `name` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `code_group` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `group_code` VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `tag` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `code` VARCHAR(50) NOT NULL,
    `color_hex` VARCHAR(20) NULL,
    `name` VARCHAR(50) NOT NULL,
    `active` TINYINT(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `price` INT NOT NULL,
    `description` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL,
    `sold_out` TINYINT(1) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `image_url` TEXT NULL,
    `updated_at` TIMESTAMP NOT NULL,
    `cat_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `common_code` (
    `sort_no` INT NOT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `code` VARCHAR(50) NOT NULL,
    `code_grp_id` BIGINT NOT NULL,
    `active` TINYINT(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu_tag` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `menu_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `ing` (
    `name` VARCHAR(100) NOT NULL,
    `type_id` BIGINT NOT NULL,
    `sold_out` TINYINT(1) NOT NULL,
    `kcal` DECIMAL(8,2) NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `protein_g` DECIMAL(8,2) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu_nutr` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `protein_g` DECIMAL(8,2) NULL,
    `fat_g` DECIMAL(8,2) NULL,
    `source_id` BIGINT NULL,
    `menu_id` BIGINT NOT NULL,
    `kcal` DECIMAL(8,2) NULL,
    `carb_g` DECIMAL(8,2) NULL,
    `sodium_mg` DECIMAL(8,2) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `opt_group` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `group_type_id` BIGINT NOT NULL,
    `max_select` INT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `min_select` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `orders` (
    `canceled_at` TIMESTAMP NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `status_id` BIGINT NOT NULL,
    `total_price` INT NOT NULL,
    `order_no` VARCHAR(50) NOT NULL,
    `order_type_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `pay_method_cfg` (
    `sort_no` INT NOT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `method_id` BIGINT NOT NULL,
    `active` TINYINT(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `ing_allergen` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `ing_id` BIGINT NOT NULL,
    `allergen_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu_ing` (
    `unit_id` BIGINT NULL,
    `is_default` TINYINT(1) NOT NULL,
    `sort_no` INT NOT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `menu_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `can_remove` TINYINT(1) NOT NULL,
    `ing_id` BIGINT NOT NULL,
    `quantity` DECIMAL(8,2) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `opt_item` (
    `created_at` TIMESTAMP NOT NULL,
    `amount` DECIMAL(8,2) NULL,
    `unit_id` BIGINT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `add_price` INT NOT NULL,
    `color_hex` VARCHAR(20) NULL,
    `opt_group_id` BIGINT NOT NULL,
    `sold_out` TINYINT(1) NOT NULL,
    `updated_at` TIMESTAMP NOT NULL,
    `list_price` INT NULL,
    `name` VARCHAR(100) NOT NULL,
    `icon_url` TEXT NULL,
    `ing_id` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `opt_policy` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `policy_key` CHAR(64) NOT NULL,
    `name` VARCHAR(120) NOT NULL,
    `min_select` INT NOT NULL,
    `max_select` INT NOT NULL,
    `item_count` INT NOT NULL,
    `menu_count` INT NOT NULL,
    `created_at` TIMESTAMP NOT NULL,
    `updated_at` TIMESTAMP NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `required` TINYINT(1) NOT NULL,
    `opt_group_id` BIGINT NOT NULL,
    `sort_no` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    `price` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `payment` (
    `refunded_at` TIMESTAMP NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `status_id` BIGINT NOT NULL,
    `paid_at` TIMESTAMP NULL,
    `method_id` BIGINT NOT NULL,
    `amount` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu_opt_override` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `menu_id` BIGINT NOT NULL,
    `note` VARCHAR(255) NULL,
    `is_default` TINYINT(1) NULL,
    `opt_item_id` BIGINT NOT NULL,
    `sort_no` INT NULL,
    `active` TINYINT(1) NULL,
    `recommended` TINYINT(1) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `opt_item_comp` (
    `sort_no` INT NOT NULL,
    `id` BIGINT NOT NULL PRIMARY KEY,
    `quantity` DECIMAL(8,2) NULL,
    `ing_id` BIGINT NULL,
    `opt_item_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `unit_id` BIGINT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `menu_opt_policy` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `menu_id` BIGINT NOT NULL,
    `policy_id` BIGINT NOT NULL,
    `priority` INT NOT NULL,
    `required` TINYINT(1) NOT NULL,
    `sort_no` INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `opt_policy_item` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `policy_id` BIGINT NOT NULL,
    `active` TINYINT(1) NOT NULL,
    `is_default` TINYINT(1) NOT NULL,
    `opt_item_id` BIGINT NOT NULL,
    `sort_no` INT NOT NULL,
    `recommended` TINYINT(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `item_exclusion` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `order_item_id` BIGINT NOT NULL,
    `ing_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `order_item_option` (
    `id` BIGINT NOT NULL PRIMARY KEY,
    `order_item_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `price` INT NOT NULL,
    `opt_item_id` BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- Foreign Key Constraints
-- =============================================

ALTER TABLE `common_code` ADD CONSTRAINT `fk_common_code_code_grp_id`
    FOREIGN KEY (`code_grp_id`) REFERENCES `code_group` (`id`);

ALTER TABLE `ing` ADD CONSTRAINT `fk_ing_type_id`
    FOREIGN KEY (`type_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `ing_allergen` ADD CONSTRAINT `fk_ing_allergen_ing_id`
    FOREIGN KEY (`ing_id`) REFERENCES `ing` (`id`);

ALTER TABLE `ing_allergen` ADD CONSTRAINT `fk_ing_allergen_allergen_id`
    FOREIGN KEY (`allergen_id`) REFERENCES `allergen` (`id`);

ALTER TABLE `item_exclusion` ADD CONSTRAINT `fk_item_exclusion_order_item_id`
    FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`);

ALTER TABLE `item_exclusion` ADD CONSTRAINT `fk_item_exclusion_ing_id`
    FOREIGN KEY (`ing_id`) REFERENCES `ing` (`id`);

ALTER TABLE `menu` ADD CONSTRAINT `fk_menu_cat_id`
    FOREIGN KEY (`cat_id`) REFERENCES `category` (`id`);

ALTER TABLE `menu_ing` ADD CONSTRAINT `fk_menu_ing_unit_id`
    FOREIGN KEY (`unit_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `menu_ing` ADD CONSTRAINT `fk_menu_ing_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `menu_ing` ADD CONSTRAINT `fk_menu_ing_role_id`
    FOREIGN KEY (`role_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `menu_ing` ADD CONSTRAINT `fk_menu_ing_ing_id`
    FOREIGN KEY (`ing_id`) REFERENCES `ing` (`id`);

ALTER TABLE `menu_nutr` ADD CONSTRAINT `fk_menu_nutr_source_id`
    FOREIGN KEY (`source_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `menu_nutr` ADD CONSTRAINT `fk_menu_nutr_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `menu_opt_override` ADD CONSTRAINT `fk_menu_opt_override_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `menu_opt_override` ADD CONSTRAINT `fk_menu_opt_override_opt_item_id`
    FOREIGN KEY (`opt_item_id`) REFERENCES `opt_item` (`id`);

ALTER TABLE `menu_opt_policy` ADD CONSTRAINT `fk_menu_opt_policy_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `menu_opt_policy` ADD CONSTRAINT `fk_menu_opt_policy_policy_id`
    FOREIGN KEY (`policy_id`) REFERENCES `opt_policy` (`id`);

ALTER TABLE `menu_tag` ADD CONSTRAINT `fk_menu_tag_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `menu_tag` ADD CONSTRAINT `fk_menu_tag_tag_id`
    FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`);

ALTER TABLE `opt_group` ADD CONSTRAINT `fk_opt_group_group_type_id`
    FOREIGN KEY (`group_type_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `opt_item` ADD CONSTRAINT `fk_opt_item_unit_id`
    FOREIGN KEY (`unit_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `opt_item` ADD CONSTRAINT `fk_opt_item_opt_group_id`
    FOREIGN KEY (`opt_group_id`) REFERENCES `opt_group` (`id`);

ALTER TABLE `opt_item` ADD CONSTRAINT `fk_opt_item_ing_id`
    FOREIGN KEY (`ing_id`) REFERENCES `ing` (`id`);

ALTER TABLE `opt_item_comp` ADD CONSTRAINT `fk_opt_item_comp_ing_id`
    FOREIGN KEY (`ing_id`) REFERENCES `ing` (`id`);

ALTER TABLE `opt_item_comp` ADD CONSTRAINT `fk_opt_item_comp_opt_item_id`
    FOREIGN KEY (`opt_item_id`) REFERENCES `opt_item` (`id`);

ALTER TABLE `opt_item_comp` ADD CONSTRAINT `fk_opt_item_comp_unit_id`
    FOREIGN KEY (`unit_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `opt_policy` ADD CONSTRAINT `fk_opt_policy_opt_group_id`
    FOREIGN KEY (`opt_group_id`) REFERENCES `opt_group` (`id`);

ALTER TABLE `opt_policy_item` ADD CONSTRAINT `fk_opt_policy_item_policy_id`
    FOREIGN KEY (`policy_id`) REFERENCES `opt_policy` (`id`);

ALTER TABLE `opt_policy_item` ADD CONSTRAINT `fk_opt_policy_item_opt_item_id`
    FOREIGN KEY (`opt_item_id`) REFERENCES `opt_item` (`id`);

ALTER TABLE `order_item` ADD CONSTRAINT `fk_order_item_order_id`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

ALTER TABLE `order_item` ADD CONSTRAINT `fk_order_item_menu_id`
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`);

ALTER TABLE `order_item_option` ADD CONSTRAINT `fk_order_item_option_order_item_id`
    FOREIGN KEY (`order_item_id`) REFERENCES `order_item` (`id`);

ALTER TABLE `order_item_option` ADD CONSTRAINT `fk_order_item_option_opt_item_id`
    FOREIGN KEY (`opt_item_id`) REFERENCES `opt_item` (`id`);

ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_status_id`
    FOREIGN KEY (`status_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `orders` ADD CONSTRAINT `fk_orders_order_type_id`
    FOREIGN KEY (`order_type_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `pay_method_cfg` ADD CONSTRAINT `fk_pay_method_cfg_method_id`
    FOREIGN KEY (`method_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `payment` ADD CONSTRAINT `fk_payment_order_id`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

ALTER TABLE `payment` ADD CONSTRAINT `fk_payment_status_id`
    FOREIGN KEY (`status_id`) REFERENCES `common_code` (`id`);

ALTER TABLE `payment` ADD CONSTRAINT `fk_payment_method_id`
    FOREIGN KEY (`method_id`) REFERENCES `common_code` (`id`);

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- End of script
-- =============================================