-- 1. complaint_category 신규 코드 추가 (약정 만료 해지, 중복 서비스 정리)
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('CONTRACT_END',  'complaint_category'),
    ('DUPLICATE_SVC', 'complaint_category');

-- 2. defense_category 신규 코드 추가 (재약정 혜택, 포인트 지급, 요금제 변경 제안)
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('CONTRACT_RENEW', 'defense_category'),
    ('LOYALTY_POINT',  'defense_category'),
    ('PLAN_CHANGE',    'defense_category');

-- 3. outbound_category 신규 코드 추가 (기타 항목 누락 방지)
-- AI 프롬프트에서 활용하는 'OTHER'가 DB에 없어 발생하는 FK 오류를 방지합니다.
INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('OTHER', 'outbound_category');