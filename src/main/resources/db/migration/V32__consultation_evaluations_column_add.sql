-- 1. 상태 컬럼 추가
ALTER TABLE consultation_evaluations 
ADD COLUMN selection_status VARCHAR(20) DEFAULT 'PENDING' NOT NULL COMMENT '선정 상태: PENDING, SELECTED, REJECTED';

-- 2. 복합 인덱스 생성 (후보 여부 + 상태값)
CREATE INDEX idx_eval_candidate_status ON consultation_evaluations (is_candidate, selection_status);