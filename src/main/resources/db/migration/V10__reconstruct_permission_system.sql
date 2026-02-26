-- ============================================================
-- V10: 권한 관리 구조 개선 (직무 기반 권한 통합)
-- ============================================================

-- 1) 기존 불필요 권한 테이블 제거
DROP TABLE IF EXISTS emp_permissions;
DROP TABLE IF EXISTS dept_permissions;

-- 2) job_role_permissions 테이블 생성
CREATE TABLE job_role_permissions (
    role_perm_id   INT AUTO_INCREMENT PRIMARY KEY  COMMENT '직무 권한 PK',
    job_role_id    INT NOT NULL                    COMMENT '직무 ID (FK)',
    perm_id        INT NOT NULL                    COMMENT '권한 ID (FK)',
    assigned_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '권한 부여일',
    -- 프로젝트 정책에 따라 is_deleted 유지 혹은 제거 결정
    is_deleted     TINYINT(1) DEFAULT 0            COMMENT '0: 사용, 1: 삭제',
    
    CONSTRAINT fk_role_perm_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles (job_role_id),
    CONSTRAINT fk_role_perm_permission FOREIGN KEY (perm_id) REFERENCES permissions (perm_id),
    
    -- 💡 중복 데이터 방지를 위한 유니크 제약 조건 추가
    UNIQUE KEY uk_job_role_permission (job_role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;