-- V4__init_risk_filter_tables.sql

/* =========================================================
   14. risk_level_policy
   ========================================================= */
CREATE TABLE risk_level_policy (
    level_code VARCHAR(20) NOT NULL COMMENT 'LOW, MEDIUM, HIGH, CRITICAL 등',
    level_name VARCHAR(50) NOT NULL COMMENT '실무용 명칭',
    sort_order INT NULL COMMENT '등급 노출 순서',
    description TEXT NULL COMMENT '등급 설명',
    PRIMARY KEY (level_code)
) COMMENT='위험 등급 마스터 테이블';


/* =========================================================
   15. risk_type_policy
   ========================================================= */
CREATE TABLE risk_type_policy (
    type_code VARCHAR(20) NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    applied_policy_id BIGINT NULL,
    PRIMARY KEY (type_code),
    CONSTRAINT fk_risk_type_policy
        FOREIGN KEY (applied_policy_id)
        REFERENCES customer_policy(policy_id)
) COMMENT='위험 유형 마스터';


/* =========================================================
   18. filter
   ========================================================= */
CREATE TABLE filter (
    filter_id INT NOT NULL AUTO_INCREMENT,
    filter_key VARCHAR(50) NOT NULL,
    alt_code VARCHAR(50) NULL,
    filter_name VARCHAR(50) NOT NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (filter_id),
    UNIQUE KEY UQ_filter_key (filter_key)
) COMMENT='검색/필터 조건 원본';


/* =========================================================
   31. filter_groups
   ========================================================= */
CREATE TABLE filter_groups (
    filter_group_id INT NOT NULL AUTO_INCREMENT,
    emp_id INT NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT(1) DEFAULT 0,
    PRIMARY KEY (filter_group_id),
    CONSTRAINT fk_filter_group_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id)
) COMMENT='필터 그룹';


/* =========================================================
   32. filter_custom
   ========================================================= */
CREATE TABLE filter_custom (
    filter_custom_id INT NOT NULL AUTO_INCREMENT,
    filter_group_id INT NOT NULL,
    filter_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    filter_value TEXT NOT NULL,
    PRIMARY KEY (filter_custom_id),
    CONSTRAINT fk_filter_custom_group
        FOREIGN KEY (filter_group_id)
        REFERENCES filter_groups(filter_group_id),
    CONSTRAINT fk_filter_custom_filter
        FOREIGN KEY (filter_id)
        REFERENCES filter(filter_id)
) COMMENT='커스텀 필터 즐겨찾기';


/* =========================================================
   36. customer_risk_logs
   ========================================================= */
CREATE TABLE customer_risk_logs (
    log_id BIGINT NOT NULL AUTO_INCREMENT,
    consult_id BIGINT NOT NULL,
    emp_id INT NOT NULL,
    customer_id BIGINT NOT NULL,
    type_code VARCHAR(20) NOT NULL,
    level_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_risk_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results(consult_id),
    CONSTRAINT fk_risk_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id),
    CONSTRAINT fk_risk_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),
    CONSTRAINT fk_risk_type
        FOREIGN KEY (type_code)
        REFERENCES risk_type_policy(type_code),
    CONSTRAINT fk_risk_level
        FOREIGN KEY (level_code)
        REFERENCES risk_level_policy(level_code)
) COMMENT='고객 위험 발생 히스토리';

/* =========================================================
   consultation_analysis_status
   ========================================================= */
CREATE TABLE consultation_analysis_status (
    consult_id BIGINT NOT NULL COMMENT 'consultation_results.consult_id 참조',
    analysis_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING, COMPLETED, FAILED',
    keyword_match_rate DECIMAL(5,2) NOT NULL DEFAULT 0
        COMMENT '0.00~100.00',
    error_msg TEXT NULL COMMENT '배치 실패 시 원인 기록',
    last_run_at TIMESTAMP NULL COMMENT '마지막 실행 시각',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '데이터 최초 생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '상태 변경 시 자동 갱신',
    deleted_at TIMESTAMP NULL COMMENT '논리 삭제 시각',

    PRIMARY KEY (consult_id),

    CONSTRAINT fk_analysis_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results(consult_id)
) COMMENT='상담 분석 상태 관리 테이블';

/* =========================================================
   43. retention_analysis
   ========================================================= */
CREATE TABLE retention_analysis (
    consult_id BIGINT NOT NULL,
    has_intent TINYINT(1) NOT NULL,
    defense_attempted TINYINT(1) NOT NULL,
    defense_success TINYINT(1) NOT NULL,
    defense_actions JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (consult_id),
    CONSTRAINT fk_retention_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results(consult_id)
) COMMENT='AI 해지/방어 분석 결과';