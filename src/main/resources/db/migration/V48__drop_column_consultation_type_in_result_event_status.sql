-- result_event_status 테이블에서 불필요한 consultation_type 컬럼 삭제
-- 사유: 해당 정보는 로직에서 더 이상 사용하지 않음

ALTER TABLE result_event_status DROP COLUMN consultation_type;