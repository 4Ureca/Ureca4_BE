-- ============================================================
-- V34: 아웃바운드 분석 지원
-- retention_analysis에 아웃바운드 분석용 컬럼 추가
-- ============================================================

ALTER TABLE retention_analysis
    ADD COLUMN call_result ENUM('CONVERTED','REJECTED','NO_ANSWER') NULL
        COMMENT '발신 결과: 전환성공/거절/부재';

ALTER TABLE retention_analysis
    ADD COLUMN reject_reason VARCHAR(200) NULL
        COMMENT '거절 사유';

CREATE INDEX idx_retention_call_result ON retention_analysis (call_result);
