-- consultation_results: 상담 유형 컬럼 추가 (INBOUND / OUTBOUND)
ALTER TABLE consultation_results
    ADD COLUMN consultation_type VARCHAR(20) NOT NULL DEFAULT 'INBOUND'
    COMMENT '상담 유형 (INBOUND | OUTBOUND)';

-- result_event_status: 스케줄러 JOIN 성능 개선을 위한 비정규화 컬럼 추가
ALTER TABLE result_event_status
    ADD COLUMN consultation_type VARCHAR(20) NOT NULL DEFAULT 'INBOUND'
    COMMENT '상담 유형 (INBOUND | OUTBOUND)';