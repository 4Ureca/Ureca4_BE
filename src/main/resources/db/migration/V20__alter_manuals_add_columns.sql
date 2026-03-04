-- ============================================================
-- Migration: V20__alter_manuals_add_columns
-- 작성일: 2026-03-02
-- 대상 테이블: manuals
-- 변경 목적: 매뉴얼 관리 고도화를 위해 category, tags, 대상 고객 유형,
--            작성자, 상태, 연관 매뉴얼 컬럼 및 FK·인덱스 추가
-- ============================================================

-- category: NOT NULL이므로 기존 데이터 보호를 위해 DEFAULT 지정 후 추가
ALTER TABLE manuals
    ADD COLUMN category VARCHAR(50)
        NOT NULL DEFAULT '미분류'
        COMMENT '분류 예: 요금정책, 해지방어, 민원대응'
        AFTER updated_at;

-- tags: JSON NULL, 기존 데이터 영향 없음
ALTER TABLE manuals
    ADD COLUMN tags JSON
        NULL
        COMMENT '검색용 태그 배열. 예: ["해지방어","VIP"]'
        AFTER category;

-- target_customer_type: NULL 허용, 기존 데이터 영향 없음
ALTER TABLE manuals
    ADD COLUMN target_customer_type VARCHAR(20)
        NULL DEFAULT 'ALL'
        COMMENT 'ALL/VIP/VVIP/DIAMOND/일반'
        AFTER tags;

-- 추후 수정 지점: created_by는 NOT NULL이지만 기존 데이터가 있을 경우
--           NULL로 추가 후 데이터 백필(backfill) 처리가 필요.
--           백필 완료 후 NOT NULL 제약을 별도 ALTER로 추가.
ALTER TABLE manuals
    ADD COLUMN created_by INT
        NULL
        COMMENT 'employees.emp_id 참조 (작성 담당자)'
        AFTER target_customer_type;

-- status: DEFAULT 'ACTIVE' 지정으로 NOT NULL 추가 가능
ALTER TABLE manuals
    ADD COLUMN status ENUM('ACTIVE', 'DRAFT', 'ARCHIVED')
        NOT NULL DEFAULT 'ACTIVE'
        COMMENT '매뉴얼 상태'
        AFTER created_by;

-- related_manual_ids: JSON NULL, 기존 데이터 영향 없음
ALTER TABLE manuals
    ADD COLUMN related_manual_ids JSON
        NULL
        COMMENT '연관 매뉴얼 ID 배열. 예: [2,7,11]'
        AFTER status;

-- 외래키 추가 (created_by → employees)
ALTER TABLE manuals
    ADD CONSTRAINT fk_manuals_created_by
        FOREIGN KEY (created_by)
        REFERENCES employees (emp_id);

-- 인덱스 추가
CREATE INDEX idx_manuals_category ON manuals (category);
CREATE INDEX idx_manuals_status   ON manuals (status);
CREATE INDEX idx_manuals_created  ON manuals (created_at DESC);
