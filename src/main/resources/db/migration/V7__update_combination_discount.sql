-- V7__update_combination_discount.sql
-- combination_discount 테이블 보정

/* =========================================================
   1. max_discount 컬럼 NULLABLE 로 변경
   ========================================================= */
ALTER TABLE combination_discount MODIFY COLUMN max_discount INT NULL;

/* =========================================================
   2. 헬로비전 결합 삭제
   ========================================================= */
DELETE FROM combination_discount WHERE comb_code = 'BND-HELLO';

/* =========================================================
   3. 시니어 가족결합 → 시니어 플러스 수정 (max_discount = NULL)
   ========================================================= */
UPDATE combination_discount
SET comb_name    = '시니어 플러스',
    composition  = '만65세 이상 포함 가족 + 인터넷',
    max_discount = NULL,
    max_lines    = '모바일1+인터넷1',
    description  = '시니어 할인 추가'
WHERE comb_code = 'BND-FMLY-SR';

/* =========================================================
   4. 인터넷 끼리 결합 max_discount → NULL
   ========================================================= */
UPDATE combination_discount
SET max_discount = NULL
WHERE comb_code = 'BND-INET2';

/* =========================================================
   5. 참 쉬운 케이블 가족 결합 추가
   ========================================================= */
INSERT INTO combination_discount
  (comb_code, comb_name, composition, max_discount, max_lines, description)
VALUES
('BND-CABLE', '참 쉬운 케이블 가족 결합', 'LG헬로비전/현대HCN/서경방송 등 케이블', 88000, '모바일10+인터넷1', '케이블 인터넷, 가족 결합 할인');