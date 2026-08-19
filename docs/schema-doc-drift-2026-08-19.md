# 스키마 문서 vs 실제 asak_db 대조표 (2026-08-19)

대상: `docs/아삭_mysql.sql`(테이블), `docs/view.sql`(뷰)
방법: 운영 DB 에 읽기 전용 접속해 `SHOW CREATE TABLE` / `SHOW CREATE VIEW` /
`information_schema` 조회. 스키마 변경은 하지 않았다.

---

# 1부. 테이블 — `docs/아삭_mysql.sql`

- 실제 운영 테이블: 26개 / 문서 기재: 25개
- 백업 테이블(문서 대상 아님): 22개

## 1-1. 테이블 존재 차이
- 문서에 없는 실제 테이블: `media_asset`
- 실제에 없는 문서 테이블: 없음

## 1-2. 테이블별 컬럼 차이

### `allergen` (차이 2건)
- 속성 누락 `id`: 실제 auto_increment
- UNIQUE 미기재: `name` (name)

### `category` (차이 5건)
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `active`: 실제 DEFAULT 1
- 컬럼 순서 불일치: 문서=sort_no, id, active, name / 실제=id, name, sort_no, active
- UNIQUE 미기재: `name` (name)

### `code_group` (차이 3건)
- 속성 누락 `id`: 실제 auto_increment
- 컬럼 순서 불일치: 문서=id, name, group_code / 실제=id, group_code, name
- UNIQUE 미기재: `group_code` (group_code)

### `common_code` (차이 7건)
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `active`: 실제 DEFAULT 1
- 컬럼 순서 불일치: 문서=sort_no, id, name, code, code_grp_id, active / 실제=id, code_grp_id, code, name, sort_no, active
- UNIQUE 미기재: `uq_common_code_group_code` (code_grp_id, code)
- FK 이름 불일치/누락: 실제 `fk_common_code_group` (code_grp_id → code_group.id)
- 문서에만 있는 FK 이름: `fk_common_code_code_grp_id`

### `ing` (차이 15건)
- 컬럼 누락: `kcal` decimal(8,2) NULL
- 컬럼 누락: `protein_g` decimal(8,2) NULL
- 컬럼 누락: `icon_asset_id` bigint NULL
- 컬럼 누락: `photo_asset_id` bigint NULL
- 기본값 누락 `sold_out`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 컬럼 순서 불일치: 문서=name, type_id, sold_out, id / 실제=id, name, type_id, sold_out, kcal, protein_g, icon_asset_id, photo_asset_id
- INDEX 미기재: `fk_ing_icon_asset_id` (icon_asset_id)
- INDEX 미기재: `fk_ing_photo_asset_id` (photo_asset_id)
- INDEX 미기재: `fk_ingredient_type` (type_id)
- UNIQUE 미기재: `name` (name)
- FK 이름 불일치/누락: 실제 `fk_ing_icon_asset_id` (icon_asset_id → media_asset.id)
- FK 이름 불일치/누락: 실제 `fk_ing_photo_asset_id` (photo_asset_id → media_asset.id)
- FK 이름 불일치/누락: 실제 `fk_ingredient_type` (type_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_ing_type_id`

### `ing_allergen` (차이 7건)
- 속성 누락 `id`: 실제 auto_increment
- INDEX 미기재: `fk_ingredient_allergen_allergen` (allergen_id)
- UNIQUE 미기재: `uq_ingredient_allergen` (ing_id, allergen_id)
- FK 이름 불일치/누락: 실제 `fk_ingredient_allergen_allergen` (allergen_id → allergen.id)
- FK 이름 불일치/누락: 실제 `fk_ingredient_allergen_ingredient` (ing_id → ing.id)
- 문서에만 있는 FK 이름: `fk_ing_allergen_ing_id`
- 문서에만 있는 FK 이름: `fk_ing_allergen_allergen_id`

### `ing_nutr` (차이 2건)
- INDEX 미기재: `fk_ing_nutr_source_id` (source_id)
- UNIQUE 미기재: `uq_ing_nutr_ing_id` (ing_id)

### `item_exclusion` (차이 7건)
- 속성 누락 `id`: 실제 auto_increment
- INDEX 미기재: `fk_item_exclusion_ingredient` (ing_id)
- UNIQUE 미기재: `uq_item_exclusion` (order_item_id, ing_id)
- FK 이름 불일치/누락: 실제 `fk_item_exclusion_ingredient` (ing_id → ing.id)
- FK 이름 불일치/누락: 실제 `fk_item_exclusion_order_item` (order_item_id → order_item.id)
- 문서에만 있는 FK 이름: `fk_item_exclusion_order_item_id`
- 문서에만 있는 FK 이름: `fk_item_exclusion_ing_id`

### `media_asset` — 문서에 아예 없음 (전체 신규 기재 필요)

### `menu` (차이 15건)
- 컬럼 누락: `image_asset_id` bigint NULL
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `price`: 실제 DEFAULT 0
- 기본값 누락 `created_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `created_at`: 실제 DEFAULT_GENERATED
- 기본값 누락 `sold_out`: 실제 DEFAULT 0
- 기본값 누락 `updated_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `updated_at`: 실제 DEFAULT_GENERATED on update CURRENT_TIMESTAMP
- 컬럼 순서 불일치: 문서=id, price, description, created_at, sold_out, name, image_url, updated_at, deleted_at, cat_id / 실제=id, cat_id, name, price, image_url, image_asset_id, description, sold_out, created_at, updated_at, deleted_at
- INDEX 미기재: `fk_menu_category` (cat_id)
- INDEX 미기재: `fk_menu_image_asset_id` (image_asset_id)
- INDEX 미기재: `idx_menu_deleted_at` (deleted_at)
- FK 이름 불일치/누락: 실제 `fk_menu_category` (cat_id → category.id)
- FK 이름 불일치/누락: 실제 `fk_menu_image_asset_id` (image_asset_id → media_asset.id)
- 문서에만 있는 FK 이름: `fk_menu_cat_id`

### `menu_ing` (차이 17건)
- 기본값 누락 `is_default`: 실제 DEFAULT 1
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `can_remove`: 실제 DEFAULT 1
- 컬럼 순서 불일치: 문서=unit_id, is_default, sort_no, id, menu_id, role_id, can_remove, ing_id, quantity / 실제=id, menu_id, ing_id, role_id, quantity, unit_id, is_default, can_remove, sort_no
- INDEX 미기재: `fk_menu_ingredient_ingredient` (ing_id)
- INDEX 미기재: `fk_menu_ingredient_role` (role_id)
- INDEX 미기재: `fk_menu_ingredient_unit` (unit_id)
- UNIQUE 미기재: `uq_menu_ingredient_role` (menu_id, ing_id, role_id)
- FK 이름 불일치/누락: 실제 `fk_menu_ingredient_ingredient` (ing_id → ing.id)
- FK 이름 불일치/누락: 실제 `fk_menu_ingredient_menu` (menu_id → menu.id)
- FK 이름 불일치/누락: 실제 `fk_menu_ingredient_role` (role_id → common_code.id)
- FK 이름 불일치/누락: 실제 `fk_menu_ingredient_unit` (unit_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_menu_ing_unit_id`
- 문서에만 있는 FK 이름: `fk_menu_ing_menu_id`
- 문서에만 있는 FK 이름: `fk_menu_ing_role_id`
- 문서에만 있는 FK 이름: `fk_menu_ing_ing_id`

### `menu_nutr` (차이 8건)
- 속성 누락 `id`: 실제 auto_increment
- 컬럼 순서 불일치: 문서=id, protein_g, fat_g, source_id, menu_id, kcal, carb_g, sodium_mg, serving_g, sugar_g, saturated_fat_g / 실제=id, menu_id, kcal, protein_g, carb_g, fat_g, sodium_mg, source_id, serving_g, sugar_g, saturated_fat_g
- INDEX 미기재: `fk_menu_nutrition_source` (source_id)
- UNIQUE 미기재: `menu_id` (menu_id)
- FK 이름 불일치/누락: 실제 `fk_menu_nutrition_menu` (menu_id → menu.id)
- FK 이름 불일치/누락: 실제 `fk_menu_nutrition_source` (source_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_menu_nutr_source_id`
- 문서에만 있는 FK 이름: `fk_menu_nutr_menu_id`

### `menu_opt_override` (차이 8건)
- 속성 누락 `id`: 실제 auto_increment
- 컬럼 순서 불일치: 문서=id, menu_id, note, is_default, opt_item_id, sort_no, active, recommended / 실제=id, menu_id, opt_item_id, recommended, is_default, sort_no, active, note
- INDEX 미기재: `fk_menu_option_override_item` (opt_item_id)
- UNIQUE 미기재: `uq_menu_option_override` (menu_id, opt_item_id)
- FK 이름 불일치/누락: 실제 `fk_menu_option_override_item` (opt_item_id → opt_item.id)
- FK 이름 불일치/누락: 실제 `fk_menu_option_override_menu` (menu_id → menu.id)
- 문서에만 있는 FK 이름: `fk_menu_opt_override_menu_id`
- 문서에만 있는 FK 이름: `fk_menu_opt_override_opt_item_id`

### `menu_opt_policy` (차이 11건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `priority`: 실제 DEFAULT 0
- 기본값 누락 `required`: 실제 DEFAULT 0
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, menu_id, policy_id, priority, required, sort_no / 실제=id, menu_id, policy_id, sort_no, required, priority
- INDEX 미기재: `fk_menu_option_policy_policy` (policy_id)
- UNIQUE 미기재: `uq_menu_option_policy` (menu_id, policy_id)
- FK 이름 불일치/누락: 실제 `fk_menu_option_policy_menu` (menu_id → menu.id)
- FK 이름 불일치/누락: 실제 `fk_menu_option_policy_policy` (policy_id → opt_policy.id)
- 문서에만 있는 FK 이름: `fk_menu_opt_policy_menu_id`
- 문서에만 있는 FK 이름: `fk_menu_opt_policy_policy_id`

### `menu_tag` (차이 7건)
- 속성 누락 `id`: 실제 auto_increment
- INDEX 미기재: `fk_menu_tag_tag` (tag_id)
- UNIQUE 미기재: `uq_menu_tag` (menu_id, tag_id)
- FK 이름 불일치/누락: 실제 `fk_menu_tag_menu` (menu_id → menu.id)
- FK 이름 불일치/누락: 실제 `fk_menu_tag_tag` (tag_id → tag.id)
- 문서에만 있는 FK 이름: `fk_menu_tag_menu_id`
- 문서에만 있는 FK 이름: `fk_menu_tag_tag_id`

### `opt_group` (차이 7건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `max_select`: 실제 DEFAULT 1
- 기본값 누락 `min_select`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, group_type_id, max_select, name, min_select / 실제=id, name, group_type_id, min_select, max_select
- INDEX 미기재: `fk_option_group_type` (group_type_id)
- FK 이름 불일치/누락: 실제 `fk_option_group_type` (group_type_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_opt_group_group_type_id`

### `opt_item` (차이 17건)
- 기본값 누락 `created_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `created_at`: 실제 DEFAULT_GENERATED
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `add_price`: 실제 DEFAULT 0
- 기본값 누락 `sold_out`: 실제 DEFAULT 0
- 기본값 누락 `updated_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `updated_at`: 실제 DEFAULT_GENERATED on update CURRENT_TIMESTAMP
- 컬럼 순서 불일치: 문서=created_at, amount, unit_id, id, add_price, color_hex, opt_group_id, sold_out, updated_at, list_price, name, icon_url, ing_id / 실제=id, opt_group_id, ing_id, name, add_price, list_price, amount, unit_id, icon_url, color_hex, sold_out, created_at, updated_at
- INDEX 미기재: `fk_option_item_group` (opt_group_id)
- INDEX 미기재: `fk_option_item_ingredient` (ing_id)
- INDEX 미기재: `fk_option_item_unit` (unit_id)
- FK 이름 불일치/누락: 실제 `fk_option_item_group` (opt_group_id → opt_group.id)
- FK 이름 불일치/누락: 실제 `fk_option_item_ingredient` (ing_id → ing.id)
- FK 이름 불일치/누락: 실제 `fk_option_item_unit` (unit_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_opt_item_unit_id`
- 문서에만 있는 FK 이름: `fk_opt_item_opt_group_id`
- 문서에만 있는 FK 이름: `fk_opt_item_ing_id`

### `opt_item_comp` (차이 12건)
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 컬럼 순서 불일치: 문서=sort_no, id, quantity, ing_id, opt_item_id, name, unit_id / 실제=id, opt_item_id, ing_id, name, quantity, unit_id, sort_no
- INDEX 미기재: `fk_option_item_component_ingredient` (ing_id)
- INDEX 미기재: `fk_option_item_component_item` (opt_item_id)
- INDEX 미기재: `fk_option_item_component_unit` (unit_id)
- FK 이름 불일치/누락: 실제 `fk_option_item_component_ingredient` (ing_id → ing.id)
- FK 이름 불일치/누락: 실제 `fk_option_item_component_item` (opt_item_id → opt_item.id)
- FK 이름 불일치/누락: 실제 `fk_option_item_component_unit` (unit_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_opt_item_comp_ing_id`
- 문서에만 있는 FK 이름: `fk_opt_item_comp_opt_item_id`
- 문서에만 있는 FK 이름: `fk_opt_item_comp_unit_id`

### `opt_policy` (차이 17건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `min_select`: 실제 DEFAULT 0
- 기본값 누락 `max_select`: 실제 DEFAULT 1
- 기본값 누락 `item_count`: 실제 DEFAULT 0
- 기본값 누락 `menu_count`: 실제 DEFAULT 0
- 기본값 누락 `created_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `created_at`: 실제 DEFAULT_GENERATED
- 기본값 누락 `updated_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `updated_at`: 실제 DEFAULT_GENERATED on update CURRENT_TIMESTAMP
- 기본값 누락 `active`: 실제 DEFAULT 1
- 기본값 누락 `required`: 실제 DEFAULT 0
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, policy_key, name, min_select, max_select, item_count, menu_count, created_at, updated_at, active, required, opt_group_id, sort_no / 실제=id, policy_key, name, opt_group_id, sort_no, required, min_select, max_select, item_count, menu_count, active, created_at, updated_at
- INDEX 미기재: `fk_option_policy_group` (opt_group_id)
- UNIQUE 미기재: `policy_key` (policy_key)
- FK 이름 불일치/누락: 실제 `fk_option_policy_group` (opt_group_id → opt_group.id)
- 문서에만 있는 FK 이름: `fk_opt_policy_opt_group_id`

### `opt_policy_item` (차이 12건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `active`: 실제 DEFAULT 1
- 기본값 누락 `is_default`: 실제 DEFAULT 0
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 기본값 누락 `recommended`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, policy_id, active, is_default, opt_item_id, sort_no, recommended / 실제=id, policy_id, opt_item_id, recommended, is_default, sort_no, active
- INDEX 미기재: `fk_option_policy_item_item` (opt_item_id)
- UNIQUE 미기재: `uq_option_policy_item` (policy_id, opt_item_id)
- FK 이름 불일치/누락: 실제 `fk_option_policy_item_item` (opt_item_id → opt_item.id)
- FK 이름 불일치/누락: 실제 `fk_option_policy_item_policy` (policy_id → opt_policy.id)
- 문서에만 있는 FK 이름: `fk_opt_policy_item_policy_id`
- 문서에만 있는 FK 이름: `fk_opt_policy_item_opt_item_id`

### `order_item` (차이 10건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `quantity`: 실제 DEFAULT 1
- 기본값 누락 `price`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, order_id, quantity, menu_id, price / 실제=id, order_id, menu_id, quantity, price
- INDEX 미기재: `fk_order_item_menu` (menu_id)
- INDEX 미기재: `fk_order_item_order` (order_id)
- FK 이름 불일치/누락: 실제 `fk_order_item_menu` (menu_id → menu.id)
- FK 이름 불일치/누락: 실제 `fk_order_item_order` (order_id → orders.id)
- 문서에만 있는 FK 이름: `fk_order_item_order_id`
- 문서에만 있는 FK 이름: `fk_order_item_menu_id`

### `order_item_option` (차이 10건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `quantity`: 실제 DEFAULT 1
- 기본값 누락 `price`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=id, order_item_id, quantity, price, opt_item_id / 실제=id, order_item_id, opt_item_id, quantity, price
- INDEX 미기재: `fk_order_item_option_option` (opt_item_id)
- UNIQUE 미기재: `uq_order_item_option` (order_item_id, opt_item_id)
- FK 이름 불일치/누락: 실제 `fk_order_item_option_item` (order_item_id → order_item.id)
- FK 이름 불일치/누락: 실제 `fk_order_item_option_option` (opt_item_id → opt_item.id)
- 문서에만 있는 FK 이름: `fk_order_item_option_order_item_id`
- 문서에만 있는 FK 이름: `fk_order_item_option_opt_item_id`

### `orders` (차이 12건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `total_price`: 실제 DEFAULT 0
- 기본값 누락 `created_at`: 실제 DEFAULT CURRENT_TIMESTAMP
- 속성 누락 `created_at`: 실제 DEFAULT_GENERATED
- 컬럼 순서 불일치: 문서=canceled_at, id, status_id, total_price, order_no, order_type_id, created_at / 실제=id, order_no, order_type_id, status_id, total_price, created_at, canceled_at
- INDEX 미기재: `fk_orders_type` (order_type_id)
- INDEX 미기재: `idx_orders_status_created_at` (status_id, created_at)
- UNIQUE 미기재: `order_no` (order_no)
- FK 이름 불일치/누락: 실제 `fk_orders_status` (status_id → common_code.id)
- FK 이름 불일치/누락: 실제 `fk_orders_type` (order_type_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_orders_status_id`
- 문서에만 있는 FK 이름: `fk_orders_order_type_id`

### `pay_method_cfg` (차이 11건)
- 컬럼 누락: `image_asset_id` bigint NULL
- 컬럼 누락: `description` varchar(100) NULL
- 기본값 누락 `sort_no`: 실제 DEFAULT 0
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `active`: 실제 DEFAULT 1
- 컬럼 순서 불일치: 문서=sort_no, id, name, method_id, active / 실제=id, method_id, name, image_asset_id, description, active, sort_no
- INDEX 미기재: `fk_pay_method_cfg_image_asset` (image_asset_id)
- UNIQUE 미기재: `method_id` (method_id)
- FK 이름 불일치/누락: 실제 `fk_pay_method_cfg_image_asset` (image_asset_id → media_asset.id)
- FK 이름 불일치/누락: 실제 `fk_payment_method_config_method` (method_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_pay_method_cfg_method_id`

### `payment` (차이 14건)
- 컬럼 누락: `idempotency_key` varchar(64) NOT NULL
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `amount`: 실제 DEFAULT 0
- 컬럼 순서 불일치: 문서=refunded_at, id, order_id, status_id, paid_at, method_id, amount / 실제=id, order_id, method_id, status_id, amount, paid_at, refunded_at, idempotency_key
- INDEX 미기재: `fk_payment_method` (method_id)
- INDEX 미기재: `fk_payment_status` (status_id)
- UNIQUE 미기재: `order_id` (order_id)
- UNIQUE 미기재: `uk_payment_idempotency_key` (idempotency_key)
- FK 이름 불일치/누락: 실제 `fk_payment_method` (method_id → common_code.id)
- FK 이름 불일치/누락: 실제 `fk_payment_order` (order_id → orders.id)
- FK 이름 불일치/누락: 실제 `fk_payment_status` (status_id → common_code.id)
- 문서에만 있는 FK 이름: `fk_payment_order_id`
- 문서에만 있는 FK 이름: `fk_payment_status_id`
- 문서에만 있는 FK 이름: `fk_payment_method_id`

### `tag` (차이 4건)
- 속성 누락 `id`: 실제 auto_increment
- 기본값 누락 `active`: 실제 DEFAULT 1
- 컬럼 순서 불일치: 문서=id, code, color_hex, name, active / 실제=id, code, name, color_hex, active
- UNIQUE 미기재: `code` (code)

---

# 2부. 뷰 — `docs/view.sql`

**결론: 뷰 문서는 실제와 어긋나지 않았다.** 테이블 쪽과 달리 손볼 내용이 없어,
검증 사실만 헤더에 기록했다.

## 2-1. 뷰 존재 차이

- 실제 뷰 22개 / 문서 기재 22개
- 문서에 없는 실제 뷰: 없음
- 실제에 없는 문서 뷰: 없음

## 2-2. 정의 대조

`SHOW CREATE VIEW` 본문을 문자열 리터럴은 보존한 채 키워드 소문자화 + 공백 정규화해서
토큰 단위로 비교했다.

| 결과 | 개수 | 뷰 |
|---|---|---|
| 완전 일치 | 20 | `vw_menu_availability`, `vw_menu_ing_detail`, `vw_menu_ing_json`, `vw_menu_opt_resolved`, `vw_order_item_base_dressing`, `vw_order_item_detail`, `vw_order_item_exclusion`, `vw_order_item_full`, `vw_order_item_option`, `vw_order_item_tag`, `vw_order_list_summary`, `vw_order_live`, `vw_order_status_summary`, `vw_order_summary`, `vw_payment_result`, `vw_sales_daily`, `vw_sales_hourly`, `vw_soldout_catalog`, `vw_top_menu_daily`, `vw_top_menu_hourly` |
| 괄호 표기만 차이 | 2 | `vw_menu_list`, `vw_menu_opt_policy_json` |
| 실제 의미 차이 | 0 | — |

괄호 차이는 MySQL 이 조인 트리에 붙이는 중첩 괄호를 문서에서 가독성을 위해 생략한 것이다.

```sql
-- 실제 (SHOW CREATE VIEW)
FROM ((`menu` `m` LEFT JOIN `menu_nutr` `mn` ON ((`mn`.`menu_id` = `m`.`id`))) ...)
-- 문서 (view.sql)
FROM `menu` `m` LEFT JOIN `menu_nutr` `mn` ON (`mn`.`menu_id` = `m`.`id`) ...
```

`=` 가 `and` 보다 먼저 묶이고 조인은 좌결합이라 파스 결과가 같다.

추론에 맡기지 않고 실제로 양쪽 SQL 을 실행해 확인했다.

| 뷰 | 행 수 (실제 / 문서) | BIT_XOR(CRC32) | SUM(CRC32) | 컬럼 순서 |
|---|---|---|---|---|
| `vw_menu_list` | 72 / 72 | 3370837307 (동일) | 144171772625 (동일) | 동일 (9개) |
| `vw_menu_opt_policy_json` | 324 / 324 | 1238921077 (동일) | 703042168873 (동일) | 동일 (11개) |

## 2-3. 뷰 건전성

22개 전부 정상 조회되고, `backup_*` 테이블을 참조하는 뷰는 없다.

| 뷰 | 컬럼 수 | 행 수 | 컬럼 |
|---|---|---|---|
| `vw_menu_availability` | 7 | 74 | menu_id, direct_sold_out, has_core_sold_out, base_ing_exhausted, base_opt_exhausted, has_blocking_standard, has_exhausted_required_group |
| `vw_menu_ing_detail` | 12 | 808 | menu_id, ing_id, ing_name, ing_sold_out, role_id, quantity, unit_id, is_default, can_remove, sort_no, allergen_id, allergen_name |
| `vw_menu_ing_json` | 11 | 411 | menu_id, ingredient_id, ing_name, ing_sold_out, role, quantity, unit, is_default, can_remove, sort_no, allergens |
| `vw_menu_list` | 9 | 72 | menu_id, category_id, name, price, image_url, base_kcal, is_sold_out, has_sold_out_ingredient, is_orderable |
| `vw_menu_opt_policy_json` | 11 | 324 | menu_id, option_group_id, name, policy_name, group_type, select_type, min_select, max_select, sort_order, is_required, items |
| `vw_menu_opt_resolved` | 14 | 6,260 | menu_id, policy_id, policy_name, min_select, max_select, policy_required, opt_item_id, opt_item_name, add_price, opt_item_sold_out, recommended, is_default, sort_no, active |
| `vw_order_item_base_dressing` | 3 | 73,786 | order_item_id, base_name, dressing_name |
| `vw_order_item_detail` | 7 | 83,188 | order_id, order_no, order_item_id, menu_id, menu_name, quantity, price |
| `vw_order_item_exclusion` | 3 | 124,190 | order_item_id, ing_id, ing_name |
| `vw_order_item_full` | 7 | 83,188 | order_id, menu_id, menu_name, quantity, unit_price, option_items, excluded_ingredients |
| `vw_order_item_option` | 6 | 349,151 | order_item_id, opt_item_id, opt_item_name, quantity, price, opt_gruop |
| `vw_order_item_tag` | 3 | 359,010 | order_item_id, tone, label |
| `vw_order_list_summary` | 13 | 51,748 | order_id, order_no, created_at, total_price, order_type_code, order_type_name, status_code, status_name, payment_status_code, payment_status_name, line_count, item_count, menu_summary |
| `vw_order_live` | 8 | 13 | order_id, order_no, order_type_label, status_code, total_price, created_at, elapsed_sec, menus |
| `vw_order_status_summary` | 6 | 1,854 | order_date, order_type_code, order_type_name, status_code, status_name, order_count |
| `vw_order_summary` | 14 | 51,748 | order_id, order_no, total_price, created_at, order_type_code, order_type_name, status_code, status_name, payment_id, paid_amount, paid_at, payment_status_code, payment_status_name, payment_method_name |
| `vw_payment_result` | 7 | 51,731 | payment_id, order_id, order_no, payment_status, approved_amount, approved_at, waiting_order_count |
| `vw_sales_daily` | 6 | 531 | sales_date, order_count, canceled_order_count, gross_sales_amount, canceled_amount, net_sales_amount |
| `vw_sales_hourly` | 7 | 6,236 | sales_date, sales_hour, order_count, canceled_order_count, gross_sales_amount, canceled_amount, net_sales_amount |
| `vw_soldout_catalog` | 6 | 323 | target_type, target_id, name, category, is_sold_out, price |
| `vw_top_menu_daily` | 6 | 22,699 | sales_date, menu_id, menu_name, quantity, order_count, sales_amount |
| `vw_top_menu_hourly` | 7 | 65,201 | sales_date, sales_hour, menu_id, menu_name, quantity, order_count, sales_amount |

## 2-4. 남은 주의점

실제 뷰는 전부 ``DEFINER=`asakasak`@`%` ``, `SQL SECURITY DEFINER` 로 만들어져 있는데,
`view.sql` 의 `CREATE OR REPLACE VIEW` 문에는 DEFINER 절이 없다. 이 파일을 그대로 적용하면
실행한 계정이 definer 가 되므로, 운영 반영 시에는 권한 계정으로 실행하거나 DEFINER 를 명시해야 한다.
문서 헤더에도 적어뒀다.
