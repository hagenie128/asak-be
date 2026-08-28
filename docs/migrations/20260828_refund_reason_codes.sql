-- 환불 사유 마스터(common_code) seed
-- 멱등: code_group·common_code가 없을 때만 INSERT

INSERT INTO `code_group` (`group_code`, `name`)
SELECT 'REFUND_REASON', '환불 사유'
WHERE NOT EXISTS (
    SELECT 1 FROM `code_group` WHERE `group_code` = 'REFUND_REASON'
);

INSERT INTO `common_code` (`code_grp_id`, `code`, `name`, `sort_no`, `active`)
SELECT g.id, v.code, v.name, v.sort_no, 1
FROM `code_group` g
JOIN (
    SELECT 'CUSTOMER_REQUEST' AS code, '고객 요청' AS name, 1 AS sort_no
    UNION ALL SELECT 'WRONG_ORDER', '잘못된 주문', 2
    UNION ALL SELECT 'OUT_OF_STOCK', '재료/메뉴 품절', 3
    UNION ALL SELECT 'DUPLICATE_PAYMENT', '중복 결제', 4
    UNION ALL SELECT 'OTHER', '기타', 99
) v
WHERE g.group_code = 'REFUND_REASON'
  AND NOT EXISTS (
      SELECT 1
      FROM `common_code` c
      WHERE c.code_grp_id = g.id
        AND c.code = v.code
  );
