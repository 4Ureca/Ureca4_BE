-- V3__init_consultation_tables.sql

/* =========================================================
   16. consultation_category_policy
   ========================================================= */
CREATE TABLE consultation_category_policy (
    category_code VARCHAR(20) NOT NULL COMMENT '카테고리 식별 코드',
    large_category VARCHAR(50) NOT NULL COMMENT '대분류명',
    medium_category VARCHAR(50) NOT NULL COMMENT '중분류명',
    small_category VARCHAR(50) NOT NULL COMMENT '소분류명',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:사용, 0:미사용',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '노출 정렬 순서',
    PRIMARY KEY (category_code)
) COMMENT='상담 카테고리 정책';


/* =========================================================
   17. manuals
   ========================================================= */
CREATE TABLE manuals (
    manual_id INT NOT NULL AUTO_INCREMENT COMMENT '메뉴얼 식별자',
    type_code VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT NOT NULL,
    is_active TINYINT(1) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '매뉴얼 생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (manual_id)
) COMMENT='유사 메뉴얼 및 대응 가이드';


/* =========================================================
   29. customer_policy
   ========================================================= */
CREATE TABLE customer_policy (
    policy_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '식별자',
    emp_id INT NOT NULL COMMENT '정책 등록 관리자',
    policy_type VARCHAR(30) NOT NULL,
    config_json JSON NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1:삭제, 0:활성',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '정책 생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    PRIMARY KEY (policy_id),
    CONSTRAINT fk_customer_policy_emp
        FOREIGN KEY (emp_id) REFERENCES employees(emp_id)
) COMMENT='고객 우대/제약 정책';


/* =========================================================
   30. consultation_results
   ========================================================= */
CREATE TABLE consultation_results (
    consult_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '식별자',
    emp_id INT NOT NULL,
    customer_id BIGINT NOT NULL,
    category_code VARCHAR(20) NOT NULL,
    duration_sec INT NOT NULL,
    consult_round INT NOT NULL DEFAULT 1,
    iam_issue TEXT NULL,
    iam_action TEXT NULL,
    iam_memo TEXT NULL,
    is_transferred TINYINT(1) NOT NULL DEFAULT 0,
    refer_id BIGINT NULL,
    group_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    contact VARCHAR(20) NOT NULL,
    PRIMARY KEY (consult_id),
    CONSTRAINT fk_consult_emp
        FOREIGN KEY (emp_id) REFERENCES employees(emp_id),
    CONSTRAINT fk_consult_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_consult_category
        FOREIGN KEY (category_code) REFERENCES consultation_category_policy(category_code),
    CONSTRAINT fk_consult_refer
        FOREIGN KEY (refer_id) REFERENCES consultation_results(consult_id),
    CONSTRAINT fk_consult_group
        FOREIGN KEY (group_id) REFERENCES consultation_results(consult_id)
) COMMENT='상담 결과서';


/* =========================================================
   40. consult_product_logs
   ========================================================= */
CREATE TABLE consult_product_logs (
    log_id INT NOT NULL AUTO_INCREMENT,
    consult_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    new_product_home VARCHAR(30) NULL,
    new_product_mobile VARCHAR(30) NULL,
    new_product_service VARCHAR(30) NULL,
    canceled_product_home VARCHAR(30) NULL,
    canceled_product_mobile VARCHAR(30) NULL,
    canceled_product_service VARCHAR(30) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_log_consult
        FOREIGN KEY (consult_id) REFERENCES consultation_results(consult_id),
    CONSTRAINT fk_log_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_log_new_home
        FOREIGN KEY (new_product_home) REFERENCES product_home(home_code),
    CONSTRAINT fk_log_new_mobile
        FOREIGN KEY (new_product_mobile) REFERENCES product_mobile(mobile_code),
    CONSTRAINT fk_log_new_service
        FOREIGN KEY (new_product_service) REFERENCES product_additional(additional_code),
    CONSTRAINT fk_log_cancel_home
        FOREIGN KEY (canceled_product_home) REFERENCES product_home(home_code),
    CONSTRAINT fk_log_cancel_mobile
        FOREIGN KEY (canceled_product_mobile) REFERENCES product_mobile(mobile_code),
    CONSTRAINT fk_log_cancel_service
        FOREIGN KEY (canceled_product_service) REFERENCES product_additional(additional_code)
) COMMENT='상담 상품 변경 로그';


/* =========================================================
   41. consultation_raw_texts
   ========================================================= */
CREATE TABLE consultation_raw_texts (
    raw_id BIGINT NOT NULL AUTO_INCREMENT,
    consult_id BIGINT NOT NULL,
    raw_text_json JSON NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (raw_id),
    CONSTRAINT fk_raw_consult
        FOREIGN KEY (consult_id) REFERENCES consultation_results(consult_id)
) COMMENT='상담 원문 데이터';


/* =========================================================
   44. client_review
   ========================================================= */
CREATE TABLE client_review (
    review_id BIGINT NOT NULL AUTO_INCREMENT,
    consult_id BIGINT NOT NULL,
    score_1 TINYINT NULL,
    score_2 TINYINT NULL,
    score_3 TINYINT NULL,
    score_4 TINYINT NULL,
    score_5 TINYINT NULL,
    personal_answer JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    PRIMARY KEY (review_id),
    CONSTRAINT fk_review_consult
        FOREIGN KEY (consult_id) REFERENCES consultation_results(consult_id)
) COMMENT='상담 종료 후 고객 만족도';