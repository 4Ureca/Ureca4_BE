-- V26__update_job_roles_and_filters.sql

/* =========================================================
   1. job_roles — 슈퍼관리자(3), 일반(4) 삭제, 상담사(1)/관리자(2)만 유지
   ========================================================= */

-- FK 제약(employee_details → job_roles) 때문에 employee_details 먼저 처리
DELETE FROM employee_details WHERE job_role_id IN (3, 4);

DELETE FROM job_roles WHERE job_role_id IN (3, 4);


/* =========================================================
   2. filter — 불필요 행 삭제
      삭제 대상: issue_keyword(7), action_keyword(8), memo_keyword(9), contract_type(16)
      FK 제약(filter_custom → filter) 때문에 filter_custom 먼저 처리
   ========================================================= */

DELETE FROM filter_custom
WHERE filter_id IN (
    SELECT filter_id FROM filter
    WHERE filter_key IN ('issue_keyword', 'action_keyword', 'memo_keyword', 'contract_type')
);

DELETE FROM filter
WHERE filter_key IN ('issue_keyword', 'action_keyword', 'memo_keyword', 'contract_type');


/* =========================================================
   3. filter — 기존 행 수정
   ========================================================= */

-- keyword: 필터 이름 변경
UPDATE filter
SET filter_name = '자율검색(상담내용 혹은 상품이름)'
WHERE filter_key = 'keyword';

-- consultant_id → consultant_name, 이름도 변경
UPDATE filter
SET filter_key  = 'consultant_name',
    filter_name = '담당 상담사 이름'
WHERE filter_key = 'consultant_id';

-- category_code → category_name
UPDATE filter
SET filter_key = 'category_name'
WHERE filter_key = 'category_code';

-- product_code → product_name, 이름도 변경
UPDATE filter
SET filter_key  = 'product_name',
    filter_name = '상품명'
WHERE filter_key = 'product_code';


/* =========================================================
   4. filter — 신규 행 추가
   ========================================================= */

INSERT INTO filter (filter_key, filter_name)
VALUES ('consult_satisfaction', '고객만족도');


/* =========================================================
   5. filter — channel filterValue 형식 주석 수정
      채널 값은 'CALL' 또는 'CHATTING' ENUM
   ========================================================= */

UPDATE filter
SET filter_name = '상담 채널 (CALL / CHATTING)'
WHERE filter_key = 'channel';


/* =========================================================
   6. client_review — score_average 컬럼 추가
      score_1 ~ score_5 의 평균점수
   ========================================================= */

ALTER TABLE client_review
ADD COLUMN score_average DOUBLE NULL COMMENT 'score_1~score_5의 평균점수'
AFTER score_5;


/* =========================================================
   최종 filter 구성 (13개)
   keyword | consult_from | consult_to | consultant_name | category_name
   channel (CALL/CHATTING) | customer_name | customer_phone | customer_type | customer_grade
   risk_type | product_name | consult_satisfaction
   ========================================================= */
