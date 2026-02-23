-- V5__init_feature_tables.sql

/* =========================================================
   33. notice
   ========================================================= */
CREATE TABLE notice (
    notice_id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    emp_id INT NOT NULL,
    is_pinned TINYINT(1) NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    status ENUM('DRAFT','SCHEDULED','ACTIVE','ARCHIVED','DELETED')
        NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visible_from DATETIME NULL,
    visible_to DATETIME NULL,

    PRIMARY KEY (notice_id),

    CONSTRAINT fk_notice_emp
        FOREIGN KEY (emp_id)
        REFERENCES employees(emp_id)
) COMMENT='사내 전체 공지';