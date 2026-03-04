-- V14__create_summary_event_status_table.sql

CREATE TABLE summary_event_status (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    consult_id      BIGINT NOT NULL COMMENT '상담 결과서 ID',
    status          ENUM('requested','completed','failed') NOT NULL COMMENT '이벤트 처리 상태',
    retry_count     INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시작시간',
    updated_at      DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정된 시간',

    CONSTRAINT fk_summary_event_status_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results (consult_id),

    -- 최신 상태 조회 최적화
    INDEX idx_summary_event_status_consult_created (consult_id, created_at DESC),
    INDEX idx_summary_event_status_status (status)

) COMMENT='상담 요약 이벤트 상태'