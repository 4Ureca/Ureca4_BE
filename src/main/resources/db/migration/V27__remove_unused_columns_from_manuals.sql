-- ============================================================
-- V27: manuals 테이블에서 AI 추출과 관련 없는 불필요한 컬럼 삭제 및 수정
-- ============================================================

-- 1. 기존 불필요 컬럼 및 이전 카테고리 컬럼 삭제
ALTER TABLE manuals DROP FOREIGN KEY fk_manuals_created_by;

ALTER TABLE manuals DROP COLUMN tags;
ALTER TABLE manuals DROP COLUMN target_customer_type;
ALTER TABLE manuals DROP COLUMN related_manual_ids;
ALTER TABLE manuals DROP COLUMN status;
ALTER TABLE manuals DROP COLUMN category;
ALTER TABLE manuals DROP COLUMN created_by;

-- 2. 새로운 연관 관계 컬럼 추가
ALTER TABLE manuals
    ADD COLUMN category_code VARCHAR(20) NOT NULL,
    ADD COLUMN emp_id INT NOT NULL;

-- 3. 외래키(Foreign Key) 제약 조건 추가

-- (1) 상담 카테고리 정책 테이블 연결
ALTER TABLE manuals
    ADD CONSTRAINT fk_manuals_category_policy
    FOREIGN KEY (category_code)
    REFERENCES consultation_category_policy (category_code);

-- (2) 직원 테이블 연결
ALTER TABLE manuals
    ADD CONSTRAINT fk_manuals_employee
    FOREIGN KEY (emp_id)
    REFERENCES employees (emp_id);

-- 4. 조회 성능 향상을 위한 인덱스 추가
CREATE INDEX idx_manuals_category_code ON manuals (category_code);
CREATE INDEX idx_manuals_emp_id ON manuals (emp_id);