-- V1__init_basic_tables.sql

/* =========================================================
   10. departments
   ========================================================= */
CREATE TABLE departments (
    dept_id INT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    dept_name VARCHAR(30) NOT NULL COMMENT '부서명(admin, consult 등)',
    location VARCHAR(225) NULL COMMENT '부서 위치',
    phone_number VARCHAR(20) NULL COMMENT '부서 대표 전화번호',

    PRIMARY KEY (dept_id)
) COMMENT='부서 정보(부서명/위치/연락처) 관리';


/* =========================================================
   11. job_roles
   ========================================================= */
CREATE TABLE job_roles (
    job_role_id INT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    role_name VARCHAR(50) NOT NULL COMMENT '상담사, 관리자, 슈퍼관리자, QA 등',
    role_desc VARCHAR(200) NULL COMMENT '해당 직무 상세 설명',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:사용, 0:미사용',

    PRIMARY KEY (job_role_id)
) COMMENT='직원의 역할(상담사/관리자 등) 정의';


/* =========================================================
   13. customer_grade
   ========================================================= */
CREATE TABLE customer_grade (
    grade_code VARCHAR(20) NOT NULL COMMENT '예: VVIP, VIP, DIAMOND',
    grade_name VARCHAR(20) NOT NULL COMMENT '프레스티지반 등',
    min_point INT NOT NULL COMMENT '등급 책정 기준값',
    priority_level INT NULL COMMENT '숫자 낮을수록 우선 배정',
    description TEXT NULL COMMENT '등급 상세 설명',

    PRIMARY KEY (grade_code)
) COMMENT='고객 등급 코드/표시명/우선순위 정의';

/* =========================================================
   20. customers
   ========================================================= */
CREATE TABLE customers (
    customer_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '테이블 식별',
    identification_num VARCHAR(30) NOT NULL COMMENT 'UUID 형태의 고객 코드',
    name VARCHAR(50) NOT NULL COMMENT '고객 이름',
    customer_type ENUM('개인','법인') NOT NULL COMMENT '고객 유형',
    gender VARCHAR(10) NULL COMMENT '성별',
    birth_date DATE NOT NULL COMMENT '생년월일',
    grade_code VARCHAR(20) NULL COMMENT 'customer_grade.grade_code 참조',
    preferred_contact VARCHAR(10) NULL COMMENT '선호 연락 수단',
    email VARCHAR(255) NULL COMMENT '이메일',
    phone VARCHAR(20) NOT NULL COMMENT '전화번호',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',

    PRIMARY KEY (customer_id),
    UNIQUE KEY UQ_customers_email (email),
    KEY IDX_customers_grade_code (grade_code),

    CONSTRAINT fk_customers_grade
        FOREIGN KEY (grade_code)
        REFERENCES customer_grade(grade_code)
) COMMENT='고객 기본 정보 관리';


/* =========================================================
   21. product_home
   ========================================================= */
CREATE TABLE product_home (
    home_code VARCHAR(30) NOT NULL COMMENT '상품 코드',
    category VARCHAR(20) NOT NULL COMMENT '인터넷/IPTV/결합상품/스마트홈',
    product_name VARCHAR(100) NOT NULL COMMENT '상품명',
    monthly_fee INT NOT NULL COMMENT '월 이용료(원)',
    speed_limit VARCHAR(20) NULL COMMENT '최대 속도',
    contract_period VARCHAR(20) NULL COMMENT '약정 기간',
    description TEXT NULL COMMENT '상품 상세 설명',

    PRIMARY KEY (home_code),
    KEY IDX_product_home_category (category)
) COMMENT='인터넷/TV 상품 마스터';

/* =========================================================
   22. product_mobile
   ========================================================= */
CREATE TABLE product_mobile (
    mobile_code VARCHAR(30) NOT NULL COMMENT '모바일 요금제 코드',
    category VARCHAR(20) NOT NULL COMMENT '요금제 분류',
    plan_name VARCHAR(100) NOT NULL COMMENT '요금제명',
    monthly_fee INT NOT NULL CHECK (monthly_fee >= 0),
    contract_fee INT NULL COMMENT '선택약정가',
    data_amount VARCHAR(20) NOT NULL COMMENT '데이터 제공량',
    qos_speed VARCHAR(20) NULL COMMENT '소진 후 속도',
    voice VARCHAR(20) NOT NULL DEFAULT '무제한',
    sms VARCHAR(20) NOT NULL DEFAULT '무제한',
    sharing_data VARCHAR(20) NULL COMMENT '테더링/쉐어링',
    benefits VARCHAR(200) NULL COMMENT '부가혜택',
    target_group VARCHAR(30) NOT NULL DEFAULT '일반' COMMENT '가입 대상',
    description TEXT NULL COMMENT '상세 설명',

    PRIMARY KEY (mobile_code),
    KEY idx_mobile_category (category),
    KEY idx_mobile_target (target_group)
) COMMENT='모바일 요금제 마스터';


/* =========================================================
   23. product_additional
   ========================================================= */
CREATE TABLE product_additional (
    additional_code VARCHAR(30) NOT NULL COMMENT '부가서비스 코드',
    category VARCHAR(20) NOT NULL COMMENT '서비스 분류',
    additional_name VARCHAR(100) NOT NULL COMMENT '서비스명',
    monthly_fee INT NOT NULL DEFAULT 0 CHECK (monthly_fee >= 0),
    description TEXT NULL COMMENT '서비스 설명',

    PRIMARY KEY (additional_code),
    KEY idx_vas_category (category)
) COMMENT='부가서비스 마스터';


/* =========================================================
   24. combination_discount
   ========================================================= */
CREATE TABLE combination_discount (
    comb_code VARCHAR(30) NOT NULL COMMENT '결합상품 코드',
    comb_name VARCHAR(100) NOT NULL COMMENT '결합 상품명',
    composition VARCHAR(500) NOT NULL COMMENT '결합 구성',
    max_discount INT NOT NULL COMMENT '최대 할인액',
    max_lines VARCHAR(50) NULL COMMENT '최대 회선 수',
    description TEXT NULL COMMENT '비고',

    PRIMARY KEY (comb_code)
) COMMENT='결합할인 상품 마스터';

/* =========================================================
   45. customer_contracts
   ========================================================= */
CREATE TABLE customer_contracts (
    contract_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '계약 고유 식별자',
    customer_id BIGINT NOT NULL COMMENT 'customers.customer_id 참조',
    combo_code VARCHAR(30) NULL COMMENT 'combination_discount.comb_code 참조',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '최초 계약 체결 시점',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일',
    extinguish_at DATETIME NULL COMMENT '계약 해지 일시',
    created_at_log TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '레코드 생성 시각',
    updated_at_log TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '레코드 수정 시각',
    deleted_at TIMESTAMP NULL COMMENT '논리 삭제 시각',

    PRIMARY KEY (contract_id),
    KEY idx_contract_customer (customer_id),
    KEY idx_contract_combo (combo_code),

    CONSTRAINT fk_contract_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),

    CONSTRAINT fk_contract_combo
        FOREIGN KEY (combo_code)
        REFERENCES combination_discount(comb_code)
) COMMENT='고객별 결합 할인 및 통합 계약 관리';


/* =========================================================
   37. customer_subscription_additional
   ========================================================= */
CREATE TABLE customer_subscription_additional (
    subscription_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '구독 이력 고유 ID',
    contract_id BIGINT NOT NULL COMMENT 'customer_contracts.contract_id 참조',
    service_code VARCHAR(30) NOT NULL COMMENT 'product_additional.additional_code 참조',
    join_date DATETIME NOT NULL COMMENT '서비스 가입 시점',
    extinguish_date DATETIME NULL COMMENT '서비스 해지 시점',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성 시각',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '데이터 수정 시각',
    deleted_at TIMESTAMP NULL COMMENT '논리 삭제 시각',

    PRIMARY KEY (subscription_id),
    KEY idx_sub_add_contract (contract_id),
    KEY idx_sub_add_service (service_code),

    CONSTRAINT fk_sub_add_contract
        FOREIGN KEY (contract_id)
        REFERENCES customer_contracts(contract_id),

    CONSTRAINT fk_sub_add_service
        FOREIGN KEY (service_code)
        REFERENCES product_additional(additional_code)
) COMMENT='고객 부가서비스 구독 이력';


/* =========================================================
   38. customer_subscription_mobile
   ========================================================= */
CREATE TABLE customer_subscription_mobile (
    asset_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '자산 고유 식별자',
    contract_id BIGINT NOT NULL COMMENT 'customer_contracts.contract_id 참조',
    mobile_code VARCHAR(30) NOT NULL COMMENT 'product_mobile.mobile_code 참조',
    joined_at DATETIME NOT NULL COMMENT '개통 일시',
    extinguish_at DATETIME NULL COMMENT '해지 일시',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    PRIMARY KEY (asset_id),
    KEY idx_sub_mobile_contract (contract_id),
    KEY idx_sub_mobile_code (mobile_code),

    CONSTRAINT fk_sub_mobile_contract
        FOREIGN KEY (contract_id)
        REFERENCES customer_contracts(contract_id),

    CONSTRAINT fk_sub_mobile_code
        FOREIGN KEY (mobile_code)
        REFERENCES product_mobile(mobile_code)
) COMMENT='고객 모바일 상품 구독 이력';


/* =========================================================
   39. customer_subscription_home
   ========================================================= */
CREATE TABLE customer_subscription_home (
    asset_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '자산 고유 식별자',
    contract_id BIGINT NOT NULL COMMENT 'customer_contracts.contract_id 참조',
    home_code VARCHAR(30) NOT NULL COMMENT 'product_home.home_code 참조',
    joined_at DATETIME NOT NULL COMMENT '개통 일시',
    extinguish_at DATETIME NULL COMMENT '해지 일시',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    PRIMARY KEY (asset_id),
    KEY idx_sub_home_contract (contract_id),
    KEY idx_sub_home_code (home_code),

    CONSTRAINT fk_sub_home_contract
        FOREIGN KEY (contract_id)
        REFERENCES customer_contracts(contract_id),

    CONSTRAINT fk_sub_home_code
        FOREIGN KEY (home_code)
        REFERENCES product_home(home_code)
) COMMENT='고객 인터넷/TV 상품 구독 이력';