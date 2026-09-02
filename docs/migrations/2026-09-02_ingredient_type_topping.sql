-- INGREDIENT_TYPE: TOPPING(토핑) 추가 및 재료 type_id 정리 (2026-09-02)
-- 운영 DB에 common_code id=59 가 이미 있으면 INSERT 구문은 생략한다.

-- INSERT INTO common_code (id, code_grp_id, code, name, sort_no, active)
-- VALUES (59, 6, 'TOPPING', '토핑', 33, 1);

-- 토핑 옵션 재료 (치즈·견과·김자반 등)
UPDATE ing SET type_id = 59 WHERE id IN (
  179, 182, 184, 189, 191, 193, 199, 204, 377, 6821
);

-- 참고: type_id > 34 이면 AdminMenuMapper가 unitName으로 분류하던 레거시 버그 수정됨.
-- ing.type_id 는 INGREDIENT_TYPE(26~31, 59)만 사용한다.
