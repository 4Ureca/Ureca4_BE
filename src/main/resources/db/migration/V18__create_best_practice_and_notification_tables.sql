-- V18__create_best_practice_and_notification_tables.sql

/* =========================================================
   50. 우수사례
   ========================================================= */
CREATE TABLE best_practices (
    bp_id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '우수사례 고유 식별자',
    consult_id     BIGINT NOT NULL COMMENT 'consultation_results.consult_id 참조',
    title          VARCHAR(200) NOT NULL COMMENT '우수사례 제목',
    description    TEXT NULL COMMENT '사례 요약 및 선정 이유',
    category       VARCHAR(50) NOT NULL COMMENT '상담 유형 분류',
    reason         TEXT NULL COMMENT '선정 이유 서술',
    created_by     INT NOT NULL COMMENT 'employees.emp_id 참조',
    is_active      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1: 게시중, 0: 숨김',
    view_count     INT NOT NULL DEFAULT 0 COMMENT '조회수',
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 시각',
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    CONSTRAINT fk_bp_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results (consult_id),

    CONSTRAINT fk_bp_created_by
        FOREIGN KEY (created_by)
        REFERENCES employees (emp_id),

    INDEX idx_bp_category (category),
    INDEX idx_bp_consult (consult_id),
    INDEX idx_bp_created (created_at DESC)
) COMMENT='관리자가 등록하는 상담 우수사례';


/* =========================================================
   51. 사용자 북마크
   ========================================================= */
CREATE TABLE user_bookmarks (
    bookmark_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '북마크 고유 식별자',
    emp_id            INT NOT NULL COMMENT 'employees.emp_id 참조',
    manual_id         INT NULL COMMENT 'manuals.manual_id 참조',
    consult_id        BIGINT NULL COMMENT 'consultation_results.consult_id 참조',
    best_practice_id  INT NULL COMMENT 'best_practices.bp_id 참조',
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '북마크 등록 시각',

    CONSTRAINT fk_bm_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees (emp_id),

    CONSTRAINT fk_bm_manual
        FOREIGN KEY (manual_id)
        REFERENCES manuals (manual_id),

    CONSTRAINT fk_bm_consult
        FOREIGN KEY (consult_id)
        REFERENCES consultation_results (consult_id),

    CONSTRAINT fk_bm_best_practice
        FOREIGN KEY (best_practice_id)
        REFERENCES best_practices (bp_id),

    INDEX idx_bm_emp (emp_id),

    UNIQUE KEY uk_bm_emp_manual (emp_id, manual_id),
    UNIQUE KEY uk_bm_emp_consult (emp_id, consult_id),
    UNIQUE KEY uk_bm_best_practice (emp_id, best_practice_id),

    CONSTRAINT chk_bm_exactly_one
    CHECK (
        (manual_id IS NOT NULL) +
        (consult_id IS NOT NULL) +
        (best_practice_id IS NOT NULL)
        = 1
    )
) COMMENT='직원 즐겨찾기 관리';


/* =========================================================
   52. 알림 수신 설정
   ========================================================= */
CREATE TABLE user_notification_settings (
    setting_id               INT AUTO_INCREMENT PRIMARY KEY COMMENT '설정 고유 식별자',
    emp_id                   INT NOT NULL COMMENT 'employees.emp_id 참조',
    notify_notice            TINYINT(1) NOT NULL DEFAULT 1 COMMENT '공지사항 알림 여부',
    notify_best_practice     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '우수사례 알림 여부',
    notify_policy_change     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '운영정책 변경 알림 여부',
    updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '설정 수정 시각',

    CONSTRAINT fk_ns_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees (emp_id),

    UNIQUE KEY uk_ns_emp (emp_id)
) COMMENT='직원 개인 알림 수신 설정';


/* =========================================================
   53. 알림 메시지
   ========================================================= */
CREATE TABLE user_notifications (
    notification_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '알림 고유 식별자',
    emp_id             INT NOT NULL COMMENT 'employees.emp_id 참조',
    notification_type  ENUM('NOTICE','BEST_PRACTICE','POLICY_CHANGE') NOT NULL COMMENT '알림 유형',
    ref_id             BIGINT NULL COMMENT '참조 콘텐츠 ID',
    message            VARCHAR(300) NOT NULL COMMENT '알림 문구',
    is_read            TINYINT(1) NOT NULL DEFAULT 0 COMMENT '읽음 여부',
    read_at            DATETIME NULL COMMENT '읽은 시각',
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발송 시각',

    CONSTRAINT fk_nf_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees (emp_id),

    INDEX idx_nf_emp_unread (emp_id, is_read),
    INDEX idx_nf_emp_created (emp_id, created_at DESC),
    INDEX idx_nf_type_ref (notification_type, ref_id)
) COMMENT='직원 알림 메시지';


/* =========================================================
   54. 공지 읽음 이력
   ========================================================= */
CREATE TABLE notice_read_log (
    log_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '읽음 이력 고유 식별자',
    notice_id   INT NOT NULL COMMENT 'notice.notice_id 참조',
    emp_id      INT NOT NULL COMMENT 'employees.emp_id 참조',
    read_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '읽은 시각',

    CONSTRAINT fk_rl_notice
        FOREIGN KEY (notice_id)
        REFERENCES notice (notice_id),

    CONSTRAINT fk_rl_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees (emp_id),

    UNIQUE KEY uk_rl_notice_emp (notice_id, emp_id),
    INDEX idx_rl_emp_unread (emp_id)
) COMMENT='공지 읽음 이력';