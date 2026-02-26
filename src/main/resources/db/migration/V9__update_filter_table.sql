-- ============================================================
-- V9: filter 시드 데이터 + hard delete 전환
-- ============================================================

-- 1) filter_groups: is_deleted 컬럼 제거 (hard delete)
ALTER TABLE filter_groups DROP COLUMN is_deleted;

-- 2) filter: is_deleted 컬럼 제거 (정책적 데이터, CRUD 미지원)
ALTER TABLE filter DROP COLUMN is_deleted;

-- 3) filter 테이블 시드 데이터 (테이블 18)
--    정책적 데이터 → 화면상 CRUD 미지원, Flyway로만 관리
INSERT INTO filter (filter_key, alt_code, filter_name) VALUES
-- ■ 기본 검색
('keyword',             NULL,                   '키워드'),
('consult_from',        NULL,                   '시작일'),
('consult_to',          NULL,                   '종료일'),
('consult_type',        'CONSULT_TYPE',         '상담유형'),
('consult_status',      'CONSULT_STATUS',       '처리상태'),
('consult_channel',     'CONSULT_CHANNEL',      '상담채널'),
('counselor',           NULL,                   '상담사'),
('department',          'DEPARTMENT',           '상담부서'),

-- ■ IAM 기반 검색
('issue',               NULL,                   'Issue'),
('action',              NULL,                   'Action'),
('memo',                NULL,                   'Memo'),

-- ■ 고객 기반 검색
('customer_info',       NULL,                   '고객정보'),
('customer_type',       'CUSTOMER_TYPE',        '고객유형'),
('customer_grade',      'CUSTOMER_GRADE',       '고객등급'),
('risk_type',           'RISK_TYPE',            '위험분류'),

-- ■ 상담 세부 조건
('duration_min',        NULL,                   '최소 소요시간'),
('duration_max',        NULL,                   '최대 소요시간'),
('priority',            'PRIORITY',             '상담 우선순위'),
('consult_difficulty',  'CONSULT_DIFFICULTY',   '상담 난이도'),
('category_large',      'CATEGORY_LARGE',       '상담 카테고리 대분류'),
('category_medium',     'CATEGORY_MEDIUM',      '상담 카테고리 중분류'),

-- ■ 이관 여부
('transfer_yn',         NULL,                   '타부서 이관 여부'),

-- ■ 상품 기반 검색
('product_subscribed',  'PRODUCT',              '가입 상품'),
('product_cancelled',   'PRODUCT',              '해지 상품'),
('product_active',      'PRODUCT',              '구독중 상품'),
('product_type',        'PRODUCT_TYPE',         '상품 타입');