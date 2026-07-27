-- =============================================================================
-- ASAK DB 탐색 / 뷰 정의 스크립트
-- -----------------------------------------------------------------------------
-- 목적:
--   1) 핵심 테이블 컬럼 구조 확인 (SHOW COLUMNS)
--   2) 주문-옵션 JOIN 관계를 단계적으로 확인
--   3) 메뉴/주문/품절/목록 API용 VIEW 생성
-- 사용법: 필요한 구간만 선택 실행 (뷰 CREATE는 운영/개발 DB에 반영됨)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- [1] 테이블 컬럼 구조 확인
--     주문·옵션·재료·메뉴·공통코드 등 핵심 테이블의 컬럼을 빠르게 본다.
-- -----------------------------------------------------------------------------

-- 주문 헤더 (주문번호, 상태, 금액 등)
SHOW COLUMNS FROM orders;

-- 주문 라인아이템 (메뉴별 수량·가격)
SHOW COLUMNS FROM order_item;

-- 주문 라인에 붙은 옵션 (베이스/드레싱/토핑 등)
SHOW COLUMNS FROM order_item_option;

-- 주문 라인에서 제외한 재료 (빼기 요청)
SHOW COLUMNS FROM item_exclusion;

-- 옵션 마스터 (옵션명, 추가금, 품절 여부)
SHOW COLUMNS FROM opt_item;

-- 재료 마스터 (재료명, 품절, 영양 등)
SHOW COLUMNS FROM ing;

-- 메뉴 마스터
SHOW COLUMNS FROM menu;

-- 공통코드 (주문유형, 상태, 재료역할 등 코드성 데이터)
SHOW COLUMNS FROM common_code;

-- 공통코드 샘플 조회 (코드값 확인용)
SELECT * FROM common_code LIMIT 40;

SHOW COLUMNS FROM common_code;

-- -----------------------------------------------------------------------------
-- [2] 주문 JOIN 관계 단계 확인
--     orders → order_item → order_item_option → opt_item 연결을 점점 넓혀 본다.
-- -----------------------------------------------------------------------------

-- 주문 + 주문아이템
SELECT * FROM orders o JOIN order_item oi ON o.id = oi.order_id;

-- 위 + 옵션 (옵션 없는 라인도 남기려고 LEFT JOIN)
SELECT *
FROM
    orders o
    JOIN order_item oi ON o.id = oi.order_id
    LEFT JOIN order_item_option oio on oio.order_item_id = oi.id;

-- 위 + 옵션 마스터(이름/추가금 등)
SELECT *
FROM
    orders o
    JOIN order_item oi ON o.id = oi.order_id
    LEFT JOIN order_item_option oio on oio.order_item_id = oi.id
    LEFT JOIN opt_item oi2 on oio.opt_item_id = oi2.id;

-- -----------------------------------------------------------------------------
-- [3] 메뉴 옵션 해석 뷰
--     메뉴에 연결된 옵션 정책 + 항목을 한 행으로 펼친다.
--     menu_opt_override가 있으면 정책 기본값보다 메뉴별 오버라이드를 우선(COALESCE).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_menu_opt_resolved AS
SELECT
    mop.menu_id AS menu_id,
    op.id AS policy_id,
    op.name AS policy_name,
    op.min_select AS min_select,
    op.max_select AS max_select,
    op.required AS policy_required,
    oi.id AS opt_item_id,
    oi.name AS opt_item_name,
    oi.add_price AS add_price,
    oi.sold_out AS opt_item_sold_out,
    COALESCE(
        mo.recommended,
        opi.recommended
    ) AS recommended,
    COALESCE(mo.is_default, opi.is_default) AS is_default,
    COALESCE(mo.sort_no, opi.sort_no) AS sort_no,
    COALESCE(mo.active, opi.active) AS active
FROM
    menu_opt_policy mop
    JOIN opt_policy op ON op.id = mop.policy_id
    JOIN opt_policy_item opi ON opi.policy_id = op.id
    JOIN opt_item oi ON oi.id = opi.opt_item_id
    LEFT JOIN menu_opt_override mo ON mo.menu_id = mop.menu_id
    AND mo.opt_item_id = opi.opt_item_id
ORDER BY mop.menu_id, sort_no;

-- -----------------------------------------------------------------------------
-- [4] 메뉴 재료 상세 뷰
--     메뉴에 들어간 재료 + 역할/수량/기본여부 + 알레르기 정보를 조인한다.
--     (알레르기가 여러 개면 재료 행이 여러 줄로 늘어날 수 있음 → JSON 버전은 아래 참고)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_menu_ing_detail AS
SELECT
    mi.menu_id,
    i.id AS ing_id,
    i.name AS ing_name,
    i.sold_out AS ing_sold_out,
    mi.role_id,
    mi.quantity,
    mi.unit_id,
    mi.is_default,
    mi.can_remove,
    mi.sort_no,
    a.id AS allergen_id,
    a.name AS allergen_name
FROM
    menu_ing mi
    JOIN ing i ON i.id = mi.ing_id
    LEFT JOIN ing_allergen ia ON ia.ing_id = i.id
    LEFT JOIN allergen a ON a.id = ia.allergen_id
ORDER BY mi.menu_id, mi.sort_no;

-- -----------------------------------------------------------------------------
-- [5] 주문 아이템 기본 상세
--     주문번호 + 메뉴명 + 수량/가격
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_order_item_detail AS
SELECT
    o.id AS order_id,
    o.order_no,
    oi.id AS order_item_id,
    oi.menu_id,
    m.name AS menu_name,
    oi.quantity,
    oi.price
FROM
    orders o
    JOIN order_item oi ON oi.order_id = o.id
    JOIN menu m ON m.id = oi.menu_id
ORDER BY o.id, oi.id;

-- -----------------------------------------------------------------------------
-- [6] 주문 아이템 옵션 / 제외재료 (정규화 행 단위)
-- -----------------------------------------------------------------------------

-- 주문 라인에 선택된 옵션 목록
CREATE OR REPLACE VIEW vw_order_item_option AS
SELECT oio.order_item_id, oio.opt_item_id, oit.name AS opt_item_name, oio.quantity, oio.price
FROM
    order_item_option oio
    JOIN opt_item oit ON oit.id = oio.opt_item_id
ORDER BY oio.order_item_id, oio.opt_item_id;

-- 주문 라인에서 제외한 재료 목록
CREATE OR REPLACE VIEW vw_order_item_exclusion AS
SELECT ie.order_item_id, ie.ing_id, i.name AS ing_name
FROM item_exclusion ie
    JOIN ing i ON i.id = ie.ing_id
ORDER BY ie.order_item_id, ie.ing_id;

-- -----------------------------------------------------------------------------
-- [7] 주문 요약 (결제 포함)
--     주문유형/상태/결제상태/결제수단을 코드명으로 풀어 보여준다.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_order_summary AS
SELECT
    o.id AS order_id,
    o.order_no,
    o.total_price,
    o.created_at,
    ot.code AS order_type_code,
    ot.name AS order_type_name,
    st.code AS status_code,
    st.name AS status_name,
    p.id AS payment_id,
    p.amount AS paid_amount,
    p.paid_at,
    ps.code AS payment_status_code,
    ps.name AS payment_status_name,
    pm.name AS payment_method_name
FROM
    orders o
    JOIN common_code ot ON ot.id = o.order_type_id
    JOIN common_code st ON st.id = o.status_id
    LEFT JOIN payment p ON p.order_id = o.id
    LEFT JOIN common_code ps ON ps.id = p.status_id
    LEFT JOIN pay_method_cfg pm ON pm.method_id = p.method_id
ORDER BY o.created_at DESC;

-- -----------------------------------------------------------------------------
-- [8] 주문 아이템 풀 상세 (옵션·제외재료를 JSON으로 묶음)
--     한 주문라인 = 1행. 하위 옵션/제외는 서브쿼리로 JSON 배열 생성.
--
--     [JSON_ARRAYAGG / JSON_OBJECT 개념]
--     - JSON_OBJECT('키', 값, '키2', 값2, ...)
--         → 한 행을 JSON 객체 1개로 만듦
--         예) JSON_OBJECT('name','아보카도','price',1000)
--             → {"name":"아보카도","price":1000}
--
--     - JSON_ARRAYAGG( 표현식 )
--         → GROUP BY 또는 서브쿼리 결과의 "여러 행"을 JSON 배열 1개로 합침
--         예) 옵션이 3행이면
--             [{"name":"아보카도",...}, {"name":"계란",...}, {"name":"베이컨",...}]
--
--     - 왜 쓰나?
--         부모 1행(주문라인)에 자식 N행(옵션)을 붙일 때,
--         JOIN만 하면 부모 행이 N배로 불어남(fan-out).
--         JSON으로 묶으면 부모는 1행 유지 + 자식은 컬럼 1개(배열)로 담김.
--         → API 응답 만들기 쉬움.
--
--     - 주의: 매칭 행이 0개면 JSON_ARRAYAGG 결과는 NULL
--         (필요하면 COALESCE(..., JSON_ARRAY()) 로 빈 배열 [] 처리)
-- -----------------------------------------------------------------------------
-- 2026-07-24 수정: Admin OrderDetailPanel.jsx / rest-api-spec.md API-007 실제 필드명에 맞춤
--   options→optionItems, exclusions→excludedIngredients, optItemId→optionItemId,
--   ingId→ingredientId, price 컬럼을 unit_price로 별칭(품목 단가 명시)
--   order_item_id 컬럼 제거 — 계약 예시에 품목 id가 없음. 서브쿼리는 oi.id를 직접 참조하므로 영향 없음.
CREATE OR REPLACE VIEW vw_order_item_full AS
SELECT
    oi.order_id,
    oi.menu_id,
    m.name AS menu_name,
    oi.quantity,
    oi.price AS unit_price,
    -- 이 주문라인의 옵션들을 [{...}, {...}] 한 컬럼으로
    (
        SELECT JSON_ARRAYAGG(
                JSON_OBJECT(
                    'optionItemId', oio.opt_item_id, 'name', oit.name, 'quantity', oio.quantity, 'price', oio.price
                )
            )
        FROM
            order_item_option oio
            JOIN opt_item oit ON oit.id = oio.opt_item_id
        WHERE
            oio.order_item_id = oi.id
    ) AS option_items,
    -- 이 주문라인의 제외재료들을 [{...}, {...}] 한 컬럼으로
    (
        SELECT JSON_ARRAYAGG(
                JSON_OBJECT(
                    'ingredientId', ie.ing_id, 'name', i.name
                )
            )
        FROM item_exclusion ie
            JOIN ing i ON i.id = ie.ing_id
        WHERE
            ie.order_item_id = oi.id
    ) AS excluded_ingredients
FROM order_item oi
    JOIN menu m ON m.id = oi.menu_id
ORDER BY oi.order_id, oi.id;

-- -----------------------------------------------------------------------------
-- [9] 메뉴 주문 가능 여부(품절 판정) 뷰
--     직접 품절 + 재료/옵션 역할별 규칙을 플래그로 계산한다.
--     최종 주문가능 여부는 vw_menu_list에서 이 플래그들을 OR 해서 판단.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_menu_availability AS
SELECT
    m.id AS menu_id,
    m.sold_out AS direct_sold_out,
    -- CORE 재료 품절 → 무조건 메뉴 품절
    EXISTS (
        SELECT 1
        FROM
            menu_ing mi
            JOIN ing i ON i.id = mi.ing_id
            JOIN common_code rc ON rc.id = mi.role_id
        WHERE
            mi.menu_id = m.id
            AND rc.code = 'CORE'
            AND i.sold_out = 1
    ) AS has_core_sold_out,
    -- BASE 재료(menu_ing 고정형): 남은 베이스가 0개면 품절 (단일/다중 동일 조건)
    EXISTS (
        SELECT mi.menu_id
        FROM
            menu_ing mi
            JOIN ing i ON i.id = mi.ing_id
            JOIN common_code rc ON rc.id = mi.role_id
        WHERE
            mi.menu_id = m.id
            AND rc.code = 'BASE'
        GROUP BY
            mi.menu_id
        HAVING
            SUM(
                CASE
                    WHEN i.sold_out = 0 THEN 1
                    ELSE 0
                END
            ) = 0
    ) AS base_ing_exhausted,
    -- BASE 옵션그룹형("베이스 변경" 등): 정책의 남은 옵션이 0개면 품절
    EXISTS (
        SELECT op.id
        FROM
            menu_opt_policy mop
            JOIN opt_policy op ON op.id = mop.policy_id
            JOIN opt_group og ON og.id = op.opt_group_id
            JOIN common_code gt ON gt.id = og.group_type_id
            JOIN opt_policy_item opi ON opi.policy_id = op.id
            JOIN opt_item oi ON oi.id = opi.opt_item_id
        WHERE
            mop.menu_id = m.id
            AND gt.code = 'BASE'
        GROUP BY
            op.id
        HAVING
            SUM(
                CASE
                    WHEN oi.sold_out = 0 THEN 1
                    ELSE 0
                END
            ) = 0
    ) AS base_opt_exhausted,
    -- STANDARD(=DEFAULT) 재료: 제거 불가인데 품절이면 메뉴 품절, 제거 가능하면 안내만
    EXISTS (
        SELECT 1
        FROM
            menu_ing mi
            JOIN ing i ON i.id = mi.ing_id
            JOIN common_code rc ON rc.id = mi.role_id
        WHERE
            mi.menu_id = m.id
            AND rc.code = 'DEFAULT'
            AND i.sold_out = 1
            AND mi.can_remove = 0
    ) AS has_blocking_standard,
    -- 필수 옵션그룹 전체 품절 (menu_opt_override 반영)
    --   활성·미품절 옵션 수가 min_select 미만이면 필수그룹 충족 불가 → 주문 불가
    EXISTS (
        SELECT mop.policy_id
        FROM
            menu_opt_policy mop
            JOIN opt_policy op ON op.id = mop.policy_id
        WHERE
            mop.menu_id = m.id
            AND (
                mop.required = 1
                OR op.required = 1
            )
            AND (
                SELECT COUNT(*)
                FROM
                    opt_policy_item opi
                    JOIN opt_item oi ON oi.id = opi.opt_item_id
                    LEFT JOIN menu_opt_override mo ON mo.menu_id = m.id
                    AND mo.opt_item_id = opi.opt_item_id
                WHERE
                    opi.policy_id = op.id
                    AND COALESCE(mo.active, opi.active) = 1
                    AND oi.sold_out = 0
            ) < op.min_select
    ) AS has_exhausted_required_group
FROM menu m;

-- -----------------------------------------------------------------------------
-- [10] 품절 관리 카탈로그
--     메뉴 / 재료 / 옵션아이템을 한 목록으로 UNION (관리 화면용)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_soldout_catalog AS
SELECT
    'MENU' AS target_type,
    m.id AS target_id,
    m.name AS name,
    c.name AS category,
    m.sold_out AS is_sold_out,
    m.price AS price
FROM menu m
    JOIN category c ON c.id = m.cat_id
UNION ALL
SELECT
    'INGREDIENT' AS target_type,
    i.id AS target_id,
    i.name AS name,
    rt.name AS category,
    i.sold_out AS is_sold_out,
    NULL AS price
FROM ing i
    JOIN common_code rt ON rt.id = i.type_id
UNION ALL
SELECT
    'OPTION_ITEM' AS target_type,
    oi.id AS target_id,
    oi.name AS name,
    og.name AS category,
    oi.sold_out AS is_sold_out,
    oi.add_price AS price
FROM opt_item oi
    JOIN opt_group og ON og.id = oi.opt_group_id;

-- -----------------------------------------------------------------------------
-- [11] 실시간 주문 보드용
--     경과 시간(elapsed_sec) 포함 — 키오스크/주방 모니터 등에서 사용
-- -----------------------------------------------------------------------------
-- vw_order_live is defined after the live-order helper views below.

-- 옵션 중 BASE/DRESSING만 따로 뽑아서 pivot (주문라인당 1행, 컬럼으로 펼침)
CREATE OR REPLACE VIEW vw_order_item_base_dressing AS
SELECT oio.order_item_id, MAX(
        CASE
            WHEN gt.code = 'BASE' THEN oit.name
        END
    ) AS base_name, MAX(
        CASE
            WHEN gt.code = 'DRESSING' THEN oit.name
        END
    ) AS dressing_name
FROM
    order_item_option oio
    JOIN opt_item oit ON oit.id = oio.opt_item_id
    JOIN opt_group og ON og.id = oit.opt_group_id
    JOIN common_code gt ON gt.id = og.group_type_id
WHERE
    gt.code IN ('BASE', 'DRESSING')
GROUP BY
    oio.order_item_id;

-- 나머지 옵션(토핑/세트사이드/세트음료) + 제외재료를 tone 붙여서 합침
--   tone: side / drink / plus / exclude — UI 태그 색·스타일 구분용
CREATE OR REPLACE VIEW vw_order_item_tag AS
SELECT
    oio.order_item_id,
    CASE gt.code
        WHEN 'SET_SIDE' THEN 'side'
        WHEN 'SET_DRINK' THEN 'drink'
        ELSE 'plus'
    END AS tone,
    oit.name AS label
FROM
    order_item_option oio
    JOIN opt_item oit ON oit.id = oio.opt_item_id
    JOIN opt_group og ON og.id = oit.opt_group_id
    JOIN common_code gt ON gt.id = og.group_type_id
WHERE
    gt.code NOT IN('BASE', 'DRESSING')
UNION ALL
SELECT ie.order_item_id, 'exclude' AS tone, i.name AS label
FROM item_exclusion ie
    JOIN ing i ON i.id = ie.ing_id;

CREATE OR REPLACE VIEW vw_order_live AS
SELECT
    o.id AS order_id,
    o.order_no,
    ot.name AS order_type_label,
    st.code AS status_code,
    o.total_price,
    o.created_at,
    TIMESTAMPDIFF(SECOND, o.created_at, NOW()) AS elapsed_sec,
    JSON_ARRAYAGG(
        JSON_OBJECT(
            'menuId', oi.menu_id,
            'menuName', m.name,
            'quantity', oi.quantity,
            'unitPrice', oi.price,
            'base', bd.base_name,
            'dressing', bd.dressing_name,
            'options', COALESCE(
                (
                    SELECT JSON_ARRAYAGG(
                        JSON_OBJECT('tone', tag.tone, 'label', tag.label)
                    )
                    FROM vw_order_item_tag tag
                    WHERE tag.order_item_id = oi.id
                ),
                JSON_ARRAY()
            )
        )
    ) AS menus
FROM orders o
    JOIN common_code ot ON ot.id = o.order_type_id
    JOIN common_code st ON st.id = o.status_id
    JOIN order_item oi ON oi.order_id = o.id
    JOIN menu m ON m.id = oi.menu_id
    LEFT JOIN vw_order_item_base_dressing bd ON bd.order_item_id = oi.id
WHERE st.code IN ('RECEIVED', 'PREPARING')
GROUP BY
    o.id,
    o.order_no,
    ot.name,
    st.code,
    o.total_price,
    o.created_at
ORDER BY o.created_at ASC;

-- -----------------------------------------------------------------------------
-- [12] 주문 집계 / 목록 요약
-- -----------------------------------------------------------------------------

-- 일자 × 주문유형 × 상태별 건수 (대시보드/리포트용)
CREATE OR REPLACE VIEW vw_order_status_summary AS
SELECT
    CAST(o.created_at AS DATE) AS order_date,
    ot.code AS order_type_code,
    ot.name AS order_type_name,
    st.code AS status_code,
    st.name AS status_name,
    COUNT(*) AS order_count
FROM
    orders o
    JOIN common_code ot ON ot.id = o.order_type_id
    JOIN common_code st ON st.id = o.status_id
GROUP BY
    order_date,
    ot.code,
    ot.name,
    st.code,
    st.name;

-- 주문 목록용 한 줄 요약
--   menu_summary: "메뉴명" 또는 "메뉴명 외 N" 형태
CREATE OR REPLACE VIEW vw_order_list_summary AS
SELECT
    o.id AS order_id,
    o.order_no,
    o.created_at,
    o.total_price,
    ot.code AS order_type_code,
    ot.name AS order_type_name,
    st.code AS status_code,
    st.name AS status_name,
    ps.code AS payment_status_code,
    ps.name AS payment_status_name,
    li.line_count,
    li.item_count,
    CASE
        WHEN li.line_count > 1 THEN CONCAT(
            li.first_menu_name,
            ' 외 ',
            li.line_count - 1
        )
        ELSE li.first_menu_name
    END AS menu_summary
FROM
    orders o
    JOIN common_code ot ON ot.id = o.order_type_id
    JOIN common_code st ON st.id = o.status_id
    LEFT JOIN payment p ON p.order_id = o.id
    LEFT JOIN common_code ps ON ps.id = p.status_id
    JOIN (
        SELECT
            order_id,
            COUNT(*) AS line_count,
            SUM(quantity) AS item_count,
            SUBSTRING_INDEX(
                GROUP_CONCAT(
                    menu_name
                    ORDER BY item_id SEPARATOR '||'
                ),
                '||',
                1
            ) AS first_menu_name
        FROM (
                SELECT oi.id AS item_id, oi.order_id, oi.quantity, m.name AS menu_name
                FROM order_item oi
                    JOIN menu m ON m.id = oi.menu_id
            ) x
        GROUP BY
            order_id
    ) li ON li.order_id = o.id;

-- -----------------------------------------------------------------------------
-- [13] 메뉴 상세 API용 JSON 묶음 뷰
--
--     JSON_ARRAYAGG 쓰는 이유 (복습):
--       재료 1개에 알레르기 N개, 옵션정책 1개에 옵션항목 N개 → 그대로 JOIN하면 행이 폭발.
--       JSON_ARRAYAGG로 자식들을 배열 컬럼 1개에 넣고, 부모는 1행 유지.
-- -----------------------------------------------------------------------------

-- 2026-07-24 수정: 프론트 실제 필드명에 맞춤
--   ing_id→ingredient_id, role_id(숫자)→role(문자 코드, 소문자), unit_id(숫자)→unit(표시 코드)
-- 재료별 알레르기를 JSON 배열로 미리 묶음 (fan-out 없이 재료당 1행)
--   allergens 예: [{"id":1,"name":"난류"},{"id":3,"name":"대두"}]
--   알레르기 0개면 서브쿼리 NULL → COALESCE로 [] 빈 배열
CREATE OR REPLACE VIEW vw_menu_ing_json AS
SELECT
    mi.menu_id,
    i.id AS ingredient_id,
    i.name AS ing_name,
    i.sold_out AS ing_sold_out,
    LOWER(rc.code) AS role,
    mi.quantity,
    ut.code AS unit,
    mi.is_default,
    mi.can_remove,
    mi.sort_no,
    COALESCE(
        (
            SELECT JSON_ARRAYAGG(
                    JSON_OBJECT('id', a.id, 'name', a.name)
                )
            FROM ing_allergen ia
                JOIN allergen a ON a.id = ia.allergen_id
            WHERE
                ia.ing_id = i.id
        ),
        JSON_ARRAY()
    ) AS allergens
FROM menu_ing mi
    JOIN ing i ON i.id = mi.ing_id
    JOIN common_code rc ON rc.id = mi.role_id
    LEFT JOIN common_code ut ON ut.id = mi.unit_id
ORDER BY mi.menu_id, mi.sort_no;

-- 2026-07-24 수정: Kiosk optionGroups[] 실제 필드명에 맞춤
--   policy_id→option_group_id, policy_name→name, optId→optionItemId, ingId→ingredientId,
--   addPrice→extraPrice, listPrice→originalPrice, amount→servingAmount, unitId(숫자)→servingUnit(코드)
--   sortNo는 프론트 계약에 없어서 제거 (그룹 레벨 sort_no만 유지)
-- 정책별 옵션 항목을 JSON 배열로 미리 묶음 (정책당 1행, override 병합 포함)
--   GROUP BY 정책 → 그 정책에 속한 옵션 행들을 JSON_ARRAYAGG로 items 컬럼에 합침
--   items 예: [{"optionItemId":10,"name":"아보카도","extraPrice":1000,...}, ...]
--   COALESCE(mo.xxx, opi.xxx): 메뉴별 override 있으면 그걸, 없으면 정책 기본값
CREATE OR REPLACE VIEW vw_menu_opt_policy_json AS
SELECT
    mop.menu_id,
    op.id AS option_group_id,
    op.name,
    gt.code AS group_type,
    CASE
        WHEN op.max_select <= 1 THEN 'SINGLE'
        ELSE 'MULTI'
    END AS select_type,
    op.min_select,
    op.max_select,
    mop.sort_no AS sort_order,
    (
        mop.required = 1
        OR op.required = 1
    ) AS is_required,
    JSON_ARRAYAGG(
        JSON_OBJECT(
            'optionItemId',
            oi.id,
            'ingredientId',
            oi.ing_id,
            'name',
            oi.name,
            'extraPrice',
            oi.add_price,
            'originalPrice',
            oi.list_price,
            'servingAmount',
            oi.amount,
            'servingUnit',
            ut.code,
            'iconUrl',
            oi.icon_url,
            'colorHex',
            oi.color_hex,
            'isSoldOut',
            oi.sold_out,
            'extraKcal',
            ing.kcal,
            'proteinG',
            ing.protein_g,
            'isRecommended',
            COALESCE(
                mo.recommended,
                opi.recommended
            ),
            'isDefault',
            COALESCE(mo.is_default, opi.is_default),
            'isActive',
            COALESCE(mo.active, opi.active)
        )
    ) AS items
FROM
    menu_opt_policy mop
    JOIN opt_policy op ON op.id = mop.policy_id
    JOIN opt_group og ON og.id = op.opt_group_id
    JOIN common_code gt ON gt.id = og.group_type_id
    JOIN opt_policy_item opi ON opi.policy_id = op.id
    JOIN opt_item oi ON oi.id = opi.opt_item_id
    LEFT JOIN ing ON ing.id = oi.ing_id
    LEFT JOIN common_code ut ON ut.id = oi.unit_id
    LEFT JOIN menu_opt_override mo ON mo.menu_id = mop.menu_id
    AND mo.opt_item_id = opi.opt_item_id
GROUP BY
    mop.menu_id,
    op.id,
    op.name,
    gt.code,
    op.max_select,
    op.min_select,
    mop.sort_no,
    mop.required,
    op.required
ORDER BY mop.menu_id, mop.sort_no;

-- -----------------------------------------------------------------------------
-- [14] 메뉴 목록 (주문 가능 여부 포함)
--     vw_menu_availability 플래그를 합쳐 is_orderable / has_sold_out_ingredient 산출
--     2026-07-24 수정: cat_id → category_id (MENU_API_CONTRACT.md 명시: "Use categoryId")
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_menu_list AS
SELECT
    m.id AS menu_id,
    m.cat_id AS category_id,
    m.name,
    m.price,
    m.image_url,
    mn.kcal AS base_kcal,
    m.sold_out AS is_sold_out,
    (
        va.has_core_sold_out
        OR va.base_ing_exhausted
        OR va.base_opt_exhausted
        OR va.has_blocking_standard
    ) AS has_sold_out_ingredient,
    NOT(
        va.direct_sold_out
        OR va.has_core_sold_out
        OR va.base_ing_exhausted
        OR va.base_opt_exhausted
        OR va.has_blocking_standard
        OR va.has_exhausted_required_group
    ) AS is_orderable
FROM
    menu m
    LEFT JOIN menu_nutr mn ON mn.menu_id = m.id
    JOIN vw_menu_availability va ON va.menu_id = m.id;

-- -----------------------------------------------------------------------------
-- [15] 메뉴 상세 헤더 조회 (뷰 아님 — menu+category 인라인 조인에 allergens/allergyText 추가)
--     재료(menu_ing) + 옵션(menu_opt_policy) 양쪽에서 알레르기를 모아 중복제거.
--     "기본 재료와 옵션 기준 자동 집계" — 프론트 주석 근거. API-003 계약: allergens[] + allergyText.
--     목록(vw_menu_list)에는 안 넣음 — 상세에서만 필요한 값을 목록 조회마다 계산하면 손해라서.
-- -----------------------------------------------------------------------------
-- SELECT m.id AS menu_id, m.cat_id AS category_id, c.name AS category_name,
--        m.name, m.price, m.image_url, m.description, m.sold_out,
--        COALESCE((
--            SELECT JSON_ARRAYAGG(name) FROM (
--                SELECT DISTINCT a.name
--                FROM menu_ing mi
--                JOIN ing_allergen ia ON ia.ing_id = mi.ing_id
--                JOIN allergen a ON a.id = ia.allergen_id
--                WHERE mi.menu_id = m.id
--                UNION
--                SELECT DISTINCT a.name
--                FROM menu_opt_policy mop
--                JOIN opt_policy_item opi ON opi.policy_id = mop.policy_id
--                JOIN opt_item oi ON oi.id = opi.opt_item_id
--                JOIN ing_allergen ia ON ia.ing_id = oi.ing_id
--                JOIN allergen a ON a.id = ia.allergen_id
--                WHERE mop.menu_id = m.id
--            ) allerg
--        ), JSON_ARRAY()) AS allergens,
--        COALESCE((
--            SELECT GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') FROM (
--                SELECT DISTINCT a.name
--                FROM menu_ing mi
--                JOIN ing_allergen ia ON ia.ing_id = mi.ing_id
--                JOIN allergen a ON a.id = ia.allergen_id
--                WHERE mi.menu_id = m.id
--                UNION
--                SELECT DISTINCT a.name
--                FROM menu_opt_policy mop
--                JOIN opt_policy_item opi ON opi.policy_id = mop.policy_id
--                JOIN opt_item oi ON oi.id = opi.opt_item_id
--                JOIN ing_allergen ia ON ia.ing_id = oi.ing_id
--                JOIN allergen a ON a.id = ia.allergen_id
--                WHERE mop.menu_id = m.id
--            ) allerg2
--        ), '') AS allergy_text
-- FROM menu m
-- JOIN category c ON c.id = m.cat_id
-- WHERE m.id = #{menuId}

-- -----------------------------------------------------------------------------
-- [16] 결제 승인 응답
--     PAYMENT_API_CONTRACT.md 계약: paymentId, orderId, orderNo, paymentStatus,
--     approvedAmount, waitingOrderCount, approvedAt.
--     waiting_order_count는 결제 시점이 아니라 조회 시점 기준 실시간 대기 건수
--     (RECEIVED/PREPARING) — 상관 서브쿼리라 조회할 때마다 다시 계산됨.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_payment_result AS
SELECT
    p.id AS payment_id,
    p.order_id,
    o.order_no,
    ps.code AS payment_status,
    p.amount AS approved_amount,
    p.paid_at AS approved_at,
    (
        SELECT COUNT(*)
        FROM orders o2
            JOIN common_code st2 ON st2.id = o2.status_id
        WHERE
            st2.code IN ('RECEIVED', 'PREPARING')
    ) AS waiting_order_count
FROM payment p
    JOIN orders o ON o.id = p.order_id
    JOIN common_code ps ON ps.id = p.status_id;

-- -----------------------------------------------------------------------------
-- [17] 결제수단 목록 (뷰 아님 — 단순 2테이블 조인이라 매퍼에 인라인)
--     PAYMENT_API_CONTRACT.md 계약: methodCode, methodName, isEnabled, sortOrder.
--     KAKAO_PAY/NAVER_PAY는 비활성이어도 화면에 표시하고 선택만 막음 → active로 필터링하지 않음.
-- -----------------------------------------------------------------------------
-- SELECT c.code AS method_code, pm.name AS method_name, pm.active AS is_enabled, pm.sort_no AS sort_order
-- FROM pay_method_cfg pm
-- JOIN common_code c ON c.id = pm.method_id
-- ORDER BY pm.sort_no;

-- -----------------------------------------------------------------------------
-- [18] 품절관리 "영향 메뉴 개수" 미리보기 (뷰 아님 — 파라미터 1개 받는 매퍼 전용 쿼리)
--     저장 전 토글 미리보기용. 실제로 sold_out을 UPDATE하지 않고 가정해서 계산.
-- -----------------------------------------------------------------------------

-- 재료 탭: 이 재료(#{ingredientId})를 품절로 바꿨을 때 막히는 메뉴 수
--   CORE는 무조건 / BASE는 이게 마지막 남은 대안이면 / DEFAULT는 제거불가면
-- SELECT COUNT(DISTINCT m.id) AS affected_menu_count
-- FROM menu m
-- JOIN menu_ing mi ON mi.menu_id = m.id
-- JOIN common_code rc ON rc.id = mi.role_id
-- WHERE mi.ing_id = #{ingredientId}
--   AND (
--       rc.code = 'CORE'
--       OR (rc.code = 'DEFAULT' AND mi.can_remove = 0)
--       OR (
--           rc.code = 'BASE'
--           AND NOT EXISTS (
--               SELECT 1
--               FROM menu_ing mi2
--               JOIN ing i2 ON i2.id = mi2.ing_id
--               WHERE mi2.menu_id = mi.menu_id
--                 AND mi2.role_id = mi.role_id
--                 AND mi2.ing_id <> #{ingredientId}
--                 AND i2.sold_out = 0
--           )
--       )
--   );

-- 옵션 탭: 이 옵션항목(#{optionItemId})을 품절로 바꿨을 때, 속한 필수그룹이 전멸하는 메뉴 수
-- SELECT COUNT(DISTINCT mop.menu_id) AS affected_menu_count
-- FROM opt_policy_item opi
-- JOIN menu_opt_policy mop ON mop.policy_id = opi.policy_id
-- JOIN opt_policy op ON op.id = opi.policy_id
-- WHERE opi.opt_item_id = #{optionItemId}
--   AND (mop.required = 1 OR op.required = 1)
--   AND (
--       SELECT COUNT(*)
--       FROM opt_policy_item opi2
--       JOIN opt_item oi2 ON oi2.id = opi2.opt_item_id
--       LEFT JOIN menu_opt_override mo2
--              ON mo2.menu_id = mop.menu_id AND mo2.opt_item_id = opi2.opt_item_id
--       WHERE opi2.policy_id = opi.policy_id
--         AND COALESCE(mo2.active, opi2.active) = 1
--         AND opi2.opt_item_id <> #{optionItemId}
--         AND oi2.sold_out = 0
--   ) < op.min_select;

-- MENU 탭은 자기 자신만 영향받으므로 이 개념 자체가 필요 없음.

-- -----------------------------------------------------------------------------
-- [19] 메타 확인용 (전체 테이블/뷰 목록)
-- -----------------------------------------------------------------------------
SHOW FULL TABLES;

SHOW full VIEWs;
