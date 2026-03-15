-- V43: analysis_code 에서 누락된 ('OTHER', 'outbound_category') 삽입
-- V42가 이미 uq_analysis_code_name 제약을 제거했으므로 정상 삽입 가능
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('OTHER', 'outbound_category');
