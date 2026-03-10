-- ============================================================
-- V28: 우수 상담 채점 시스템 구축
-- 1. excellent_event_status: AI 채점 작업 상태 추적
-- 2. consultation_evaluations: AI 분석 결과 및 점수 기록
-- 3. weekly_excellent_cases: 주간 우수사례 선정 스냅샷
-- ============================================================

-- 1. 우수 상담 평가 이벤트 상태 테이블
CREATE TABLE excellent_event_status (
    excellent_event_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    consult_id         BIGINT NOT NULL COMMENT '상담 결과서 ID 참조',
    status             ENUM('REQUESTED', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'REQUESTED' COMMENT '결과 처리 상태',
    retry_count        INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    fail_reason        TEXT NULL COMMENT '실패 원인',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    started_at         DATETIME NULL COMMENT '시작 시간',
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정된 시간',

    CONSTRAINT fk_ex_event_consult_id FOREIGN KEY (consult_id) REFERENCES consultation_results (consult_id)
);

-- excellent_event_status 인덱스
CREATE INDEX idx_excellent_event_status_consult_created ON excellent_event_status (consult_id, created_at DESC);
CREATE INDEX idx_excellent_event_status_status ON excellent_event_status (status);


-- 2. 상담 평가 결과 기록 테이블
CREATE TABLE consultation_evaluations (
    evaluation_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    consult_id         BIGINT NOT NULL COMMENT '상담 결과서 ID 참조',
    score              INT NOT NULL COMMENT 'AI가 매긴 점수(0~100)',
    evaluation_reason  TEXT NOT NULL COMMENT 'AI 평가 사유',
    is_candidate       BOOLEAN NOT NULL DEFAULT FALSE COMMENT '후보 여부',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정된 시간',

    CONSTRAINT fk_eval_consult_id FOREIGN KEY (consult_id) REFERENCES consultation_results (consult_id)
);

-- consultation_evaluations 인덱스
CREATE INDEX idx_eval_consult_created ON consultation_evaluations (consult_id, created_at DESC);


-- 3. 주간 우수 상담 사례 스냅샷 테이블
CREATE TABLE weekly_excellent_cases (
    snapshot_id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    consult_id         BIGINT NOT NULL COMMENT '상담 결과서 ID 참조',
    evaluation_id      BIGINT NOT NULL COMMENT '평가 결과 ID 참조',
    year_val           INT NOT NULL COMMENT '선정 연도',
    week_val           INT NOT NULL COMMENT '선정 주차',
    admin_reason       TEXT NOT NULL COMMENT '관리자 선정 사유',
    selected_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '선정 시간',
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',

    CONSTRAINT fk_weekly_consult_id FOREIGN KEY (consult_id) REFERENCES consultation_results (consult_id),
    CONSTRAINT fk_weekly_eval_id FOREIGN KEY (evaluation_id) REFERENCES consultation_evaluations (evaluation_id)
);

-- weekly_excellent_cases 인덱스
CREATE INDEX idx_weekly_year_week ON weekly_excellent_cases (year_val, week_val);
CREATE INDEX idx_weekly_consult_id ON weekly_excellent_cases (consult_id);