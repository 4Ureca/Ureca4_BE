-- V16__modify_consult_tables.sql

-- =========================================================
-- 30. consultation_results 수정
-- =========================================================
ALTER TABLE consultation_results
    ADD COLUMN channel ENUM('CALL','CHATTING') NOT NULL
    COMMENT '상담 채널' AFTER customer_id;

ALTER TABLE consultation_results
    DROP FOREIGN KEY fk_consult_group,
    DROP FOREIGN KEY fk_consult_refer;

ALTER TABLE consultation_results
    DROP INDEX fk_consult_group,
    DROP INDEX fk_consult_refer;

ALTER TABLE consultation_results
    DROP COLUMN consult_round,
    DROP COLUMN is_transferred,
    DROP COLUMN refer_id,
    DROP COLUMN group_id,
    DROP COLUMN contact;

-- =========================================================
-- 40. consult_product_logs 수정
-- =========================================================
ALTER TABLE consult_product_logs
    ADD COLUMN contract_type ENUM('NEW','CANCEL','CHANGE','RENEW')
    NOT NULL
    COMMENT '계약 유형'
    AFTER customer_id;

-- =========================================================
-- 43. retention_analysis 수정
-- =========================================================
ALTER TABLE retention_analysis
    ADD COLUMN complaint_reason TEXT NULL
    COMMENT '고객 불만 사유'
    AFTER has_intent;
