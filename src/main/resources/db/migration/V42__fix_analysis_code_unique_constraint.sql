-- V42: analysis_code 유니크 제약 수정 및 누락된 OTHER 코드 삽입
-- 문제: uq_analysis_code_name (code_name) 단독 UNIQUE 제약으로 인해
--       V41에서 INSERT IGNORE가 silent skip되어 ('OTHER', 'defense_category')가 삽입되지 않음.
-- 해결: 단독 UNIQUE 제약을 제거하고, 복합 UNIQUE (code_name, classification)만 유지.
--       MySQL FK는 복합 인덱스의 leftmost prefix로도 동작하므로 FK 제약은 그대로 유지됨.

-- 1. code_name 단독 UNIQUE 제약 제거
ALTER TABLE analysis_code
    DROP INDEX uq_analysis_code_name;

-- 2. V41에서 누락된 ('OTHER', 'defense_category') 삽입
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('OTHER', 'defense_category');