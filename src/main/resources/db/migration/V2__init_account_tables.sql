-- V2__init_account_tables.sql

/* =========================================================
   12. permissions
   ========================================================= */
CREATE TABLE permissions (
    perm_id INT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    perm_code VARCHAR(100) NOT NULL COMMENT 'ORDER_CREATE 등',
    perm_desc TEXT NULL COMMENT '권한 설명',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 : 1',

    PRIMARY KEY (perm_id),
    KEY idx_permissions_code_deleted (perm_code, is_deleted)
) COMMENT='모든 기능별 권한 코드 정의';


/* =========================================================
   19. employees
   ========================================================= */
CREATE TABLE employees (
    emp_id INT NOT NULL AUTO_INCREMENT COMMENT '시스템 전체 식별자',
    login_id VARCHAR(50) NOT NULL COMMENT '사번 또는 사용자 ID',
    password VARCHAR(255) NOT NULL COMMENT '암호화 필요',
    name VARCHAR(30) NOT NULL COMMENT '직원 이름',
    email VARCHAR(100) NOT NULL COMMENT '직원 이메일',
    phone VARCHAR(20) NULL COMMENT '직원 전화번호',
    birth DATE NULL COMMENT '생년월일',
    gender VARCHAR(10) NULL COMMENT 'male, female, other',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1:활성화, 0:비활성화',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 시각',

    PRIMARY KEY (emp_id),
    UNIQUE KEY UQ_employees_login_id (login_id)
) COMMENT='직원 계정/기본 프로필 관리';


/* =========================================================
   25. employee_details
   ========================================================= */
CREATE TABLE employee_details (
    emp_id INT NOT NULL COMMENT 'employees.emp_id 참조',
    dept_id INT NOT NULL COMMENT 'departments.dept_id 참조',
    job_role_id INT NOT NULL COMMENT 'job_roles.job_role_id 참조',
    joined_at DATE NULL COMMENT '입사일',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',

    PRIMARY KEY (emp_id),

    CONSTRAINT fk_emp_details_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id),

    CONSTRAINT fk_emp_details_dept
        FOREIGN KEY (dept_id)
        REFERENCES departments(dept_id),

    CONSTRAINT fk_emp_details_role
        FOREIGN KEY (job_role_id)
        REFERENCES job_roles(job_role_id)
) COMMENT='직원 업무 관련 정보';


/* =========================================================
   26. emp_permissions
   ========================================================= */
CREATE TABLE emp_permissions (
    emp_perm_id INT NOT NULL AUTO_INCREMENT COMMENT '식별자',
    emp_id INT NOT NULL COMMENT 'employees.emp_id 참조',
    perm_id INT NOT NULL COMMENT 'permissions.perm_id 참조',
    assigned_at DATETIME NULL COMMENT '권한 부여일',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1:삭제',

    PRIMARY KEY (emp_perm_id),

    CONSTRAINT fk_emp_perm_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id),

    CONSTRAINT fk_emp_perm_perm
        FOREIGN KEY (perm_id)
        REFERENCES permissions(perm_id)
) COMMENT='직원 개별 권한';


/* =========================================================
   27. dept_permissions
   ========================================================= */
CREATE TABLE dept_permissions (
    id INT NOT NULL AUTO_INCREMENT COMMENT '식별자',
    dept_id INT NOT NULL COMMENT 'departments.dept_id 참조',
    perm_id INT NOT NULL COMMENT 'permissions.perm_id 참조',
    assigned_at DATETIME NULL COMMENT '부서 권한 부여일',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제: 1',

    PRIMARY KEY (id),

    CONSTRAINT fk_dept_perm_dept
        FOREIGN KEY (dept_id)
        REFERENCES departments(dept_id),

    CONSTRAINT fk_dept_perm_perm
        FOREIGN KEY (perm_id)
        REFERENCES permissions(perm_id)
) COMMENT='부서 단위 권한';


/* =========================================================
   28. refresh_token
   ========================================================= */
CREATE TABLE refresh_token (
    refresh_token_id INT NOT NULL AUTO_INCREMENT COMMENT '식별자',
    emp_id INT NOT NULL COMMENT 'employees.emp_id 참조',
    refresh_token VARCHAR(255) NOT NULL COMMENT '리프레시 토큰 값',
    expired_at DATETIME NOT NULL COMMENT '만료 시각',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',

    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY UQ_refresh_token (refresh_token),
    KEY idx_refresh_emp (emp_id),

    CONSTRAINT fk_refresh_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id)
) COMMENT='직원 로그인 리프레시 토큰 관리';