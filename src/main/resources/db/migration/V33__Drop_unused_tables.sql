-- 1. 자식 테이블(user_bookmarks)에 걸려 있는 외래 키 제약 조건 먼저 삭제
ALTER TABLE user_bookmarks DROP FOREIGN KEY fk_bm_best_practice;

-- 2. 이제 부모 테이블 삭제 가능
DROP TABLE IF EXISTS best_practices;

-- 3. 기존에 사용하던 상담 분석 상태 테이블 삭제
DROP TABLE IF EXISTS consultation_analysis_status;