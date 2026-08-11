-- Soft delete support for menu (2026-08-11)
-- ORDER history (order_item.menu_id) must keep referencing menu rows.

ALTER TABLE `menu`
  ADD COLUMN `deleted_at` TIMESTAMP NULL DEFAULT NULL AFTER `updated_at`;

CREATE INDEX `idx_menu_deleted_at` ON `menu` (`deleted_at`);

-- Also refresh vw_menu_list with: WHERE m.deleted_at IS NULL
-- See docs/view.sql
