-- ============================================================
-- Migration: V19__alter_notice_add_type_columns
-- 작성일: 2026-03-02
-- 대상 테이블: notice
-- 변경 목적: notice_type(공지 유형), target_role(대상 역할) 컬럼 및
--            관련 인덱스 추가
-- ============================================================

-- notice_type 컬럼 추가 (공지 유형 분류)
ALTER TABLE notice
    ADD COLUMN notice_type ENUM('GENERAL', 'URGENT', 'SYSTEM', 'POLICY', 'EVENT')
        NOT NULL DEFAULT 'GENERAL'
        COMMENT '공지 유형'
        AFTER visible_to;

-- target_role 컬럼 추가 (공지 대상 역할)
ALTER TABLE notice
    ADD COLUMN target_role ENUM('ALL', 'AGENT', 'ADMIN')
        NULL DEFAULT 'ALL'
        COMMENT '공지 대상 역할'
        AFTER notice_type;

-- 인덱스 추가
CREATE INDEX idx_notice_type   ON notice (notice_type);
CREATE INDEX idx_notice_status ON notice (status);
CREATE INDEX idx_notice_pinned ON notice (is_pinned, created_at DESC);
