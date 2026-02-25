-- V8__modify_tables.sql
-- 더미데이터 대상 테이블 수정

-- =========================================================
-- 1. 고객 UUID 길이 변경
-- =========================================================
ALTER TABLE crm.customers
    MODIFY COLUMN identification_num VARCHAR(36)
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    NOT NULL
    COMMENT 'UUID 형태의 고객 코드';


-- =========================================================
-- 2. customer_grade 컬럼 삭제
-- =========================================================
ALTER TABLE crm.customer_grade
    DROP COLUMN description,
    DROP COLUMN min_point;


-- =========================================================
-- 3. customer_contracts 에서 customer_id 삭제
-- =========================================================
-- FK 먼저 제거 (FK 이름은 실제 DB 기준으로 확인 필요)
ALTER TABLE crm.customer_contracts
    DROP FOREIGN KEY fk_contract_customer;

-- 컬럼 삭제
ALTER TABLE crm.customer_contracts
    DROP COLUMN customer_id;


-- =========================================================
-- 4. customer_subscription_additional 수정
-- =========================================================
ALTER TABLE crm.customer_subscription_additional
    ADD COLUMN customer_id BIGINT NOT NULL AFTER subscription_id;

ALTER TABLE crm.customer_subscription_additional
    MODIFY COLUMN contract_id BIGINT NOT NULL AFTER customer_id;

ALTER TABLE crm.customer_subscription_additional
    ADD CONSTRAINT fk_csa_customer
        FOREIGN KEY (customer_id)
        REFERENCES crm.customers(customer_id);

CREATE INDEX idx_csa_customer_id
    ON crm.customer_subscription_additional(customer_id);


-- =========================================================
-- 5. customer_subscription_mobile 수정
-- =========================================================
ALTER TABLE crm.customer_subscription_mobile
    ADD COLUMN customer_id BIGINT NOT NULL AFTER asset_id;

ALTER TABLE crm.customer_subscription_mobile
    MODIFY COLUMN contract_id BIGINT NOT NULL AFTER customer_id;

ALTER TABLE crm.customer_subscription_mobile
    ADD CONSTRAINT fk_csm_customer
        FOREIGN KEY (customer_id)
        REFERENCES crm.customers(customer_id);

CREATE INDEX idx_csm_customer_id
    ON crm.customer_subscription_mobile(customer_id);


-- =========================================================
-- 6. customer_subscription_home 수정
-- =========================================================
ALTER TABLE crm.customer_subscription_home
    ADD COLUMN customer_id BIGINT NOT NULL AFTER asset_id;

ALTER TABLE crm.customer_subscription_home
    MODIFY COLUMN contract_id BIGINT NOT NULL AFTER customer_id;

ALTER TABLE crm.customer_subscription_home
    ADD CONSTRAINT fk_csh_customer
        FOREIGN KEY (customer_id)
        REFERENCES crm.customers(customer_id);

CREATE INDEX idx_csh_customer_id
    ON crm.customer_subscription_home(customer_id);