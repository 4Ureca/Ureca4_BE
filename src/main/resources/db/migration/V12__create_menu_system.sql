-- ============================================================
-- V12: 메뉴 마스터 및 직무-메뉴 매핑 테이블 생성
-- ============================================================

-- 1) menus (메뉴 마스터 테이블) 생성
CREATE TABLE menus (
    menu_id      INT AUTO_INCREMENT PRIMARY KEY         COMMENT '메뉴 PK',
    menu_code    VARCHAR(100) NOT NULL                 COMMENT '메뉴 코드 (예: MENU_DASHBOARD)',
    menu_desc    TEXT                                   COMMENT '메뉴 상세 설명',
    is_deleted   TINYINT(1) DEFAULT 0 NOT NULL         COMMENT '0: 사용, 1: 삭제',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP    COMMENT '생성일',
    
    -- 조회 성능 및 중복 체크를 위한 인덱스
    INDEX idx_menus_code_deleted (menu_code, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 2) job_role_menus (역할군-메뉴 매핑 테이블) 생성
CREATE TABLE job_role_menus (
    role_menu_id   INT AUTO_INCREMENT PRIMARY KEY         COMMENT '매핑 PK',
    job_role_id    INT NOT NULL                           COMMENT '직무 ID (FK)',
    menu_id        INT NOT NULL                           COMMENT '메뉴 ID (FK)',
    assigned_at    DATETIME DEFAULT CURRENT_TIMESTAMP    COMMENT '권한 할당일',
    is_deleted     TINYINT(1) DEFAULT 0 NOT NULL         COMMENT '0: 사용, 1: 삭제',

    -- 외래 키 제약 조건 (job_roles와 menus 테이블 참조)
    CONSTRAINT fk_role_menu_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles (job_role_id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menus (menu_id),

    -- 동일 직무에 동일 메뉴 중복 할당 방지
    UNIQUE KEY uk_job_role_menu (job_role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;