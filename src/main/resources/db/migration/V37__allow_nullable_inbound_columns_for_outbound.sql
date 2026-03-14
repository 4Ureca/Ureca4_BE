-- V{다음번호}__allow_nullable_inbound_columns_for_outbound.sql
-- 아웃바운드 상담 기록 시, 인바운드 전용 컬럼들에 NULL을 허용하도록 변경
ALTER TABLE retention_analysis
    MODIFY COLUMN has_intent        TINYINT(1) NULL COMMENT '이탈 의사 여부 (NULL 허용)',
    MODIFY COLUMN defense_attempted TINYINT(1) NULL COMMENT '방어 시도 여부 (NULL 허용)',
    MODIFY COLUMN defense_success   TINYINT(1) NULL COMMENT '방어 성공 여부 (NULL 허용)';

