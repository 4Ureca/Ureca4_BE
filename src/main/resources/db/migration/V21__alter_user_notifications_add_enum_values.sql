-- ============================================================
-- Migration: V21__alter_user_notifications_add_enum_values
-- 작성일: 2026-03-02
-- 대상 테이블: user_notifications
-- 변경 목적: notification_type ENUM에 'EVENT', 'URGENT' 값 추가
--            (V18에서 생성된 3개 값 → 5개 값으로 확장)
-- ============================================================

ALTER TABLE user_notifications
    MODIFY COLUMN notification_type
        ENUM('NOTICE', 'BEST_PRACTICE', 'POLICY_CHANGE', 'EVENT', 'URGENT')
        NOT NULL
        COMMENT '알림 유형';
