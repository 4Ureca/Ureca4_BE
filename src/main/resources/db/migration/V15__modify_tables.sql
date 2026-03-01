-- V15__modify_manual_tables.sql

-- =========================
-- 15. risk_type_policy 수정
-- =========================
ALTER TABLE risk_type_policy
    DROP FOREIGN KEY fk_risk_type_policy;

ALTER TABLE risk_type_policy
    DROP COLUMN applied_policy_id;

-- =========================
-- 17. manuals 수정
-- =========================
ALTER TABLE manuals
    DROP COLUMN type_code;

-- =========================
-- 3. customer_policy 재생성
-- =========================
DROP TABLE IF EXISTS customer_policy;

CREATE TABLE customer_policy (
    policy_id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '정책(매뉴얼) 식별자',
    type_code          VARCHAR(20) NOT NULL COMMENT '위험 유형 코드',
    policy_code        VARCHAR(50) NOT NULL COMMENT '고정 정책 코드',
    policy_title       VARCHAR(200) NOT NULL COMMENT '정책 제목',
    policy_summary     VARCHAR(500) NULL COMMENT '정책 요약',
    severity           TINYINT NOT NULL DEFAULT 0 COMMENT '0:안내, 1:주의, 2:긴급',
    content            LONGTEXT NOT NULL COMMENT '매뉴얼/지침 본문',
    script_json        JSON NULL COMMENT '추천 응대 스크립트',
    display_start_at   TIMESTAMP NULL COMMENT '노출 시작 시점',
    display_end_at     TIMESTAMP NULL COMMENT '노출 종료 시점',
    is_active          BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부',

    CONSTRAINT fk_customer_policy_type
        FOREIGN KEY (type_code)
        REFERENCES risk_type_policy (type_code),

    CONSTRAINT uq_customer_policy_code
        UNIQUE (policy_code),

    INDEX idx_customer_policy_type (type_code),

    INDEX idx_customer_policy_list
        (type_code, is_active, severity, policy_id)

) COMMENT='고객 유형별 대응 정책';