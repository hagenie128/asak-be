-- API-006 결제 완료 후 일별 고정 대기번호 발급에 적용한 DDL 기록
-- 2026-08-29 운영 DB에는 아래 컬럼, 제약조건, 테이블이 이미 존재한다.
-- 재실행용 멱등 스크립트가 아니므로 신규 환경 구축 또는 변경 이력 확인용으로 사용한다.
-- 1. 




ALTER TABLE `orders`
  ADD COLUMN `waiting_date` DATE NULL,
  ADD COLUMN `waiting_order_no` INT NULL,
  ADD CONSTRAINT `uq_waiting_no_per_day`
    UNIQUE (`waiting_date`, `waiting_order_no`);

CREATE TABLE `daily_waiting_sequence` (
  `waiting_date` DATE NOT NULL,
  `last_waiting_order_no` INT NOT NULL,
  PRIMARY KEY (`waiting_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
