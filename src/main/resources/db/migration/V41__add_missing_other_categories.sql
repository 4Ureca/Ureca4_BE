-- analysis_code: defense 및 outbound 카테고리에 'OTHER' 코드 추가
-- AI 분석 결과 매핑 시 발생할 수 있는 외래 키(FK) 오류를 방지하기 위한 필수 데이터입니다.

INSERT IGNORE INTO analysis_code (code_name, classification) VALUES
    ('OTHER', 'defense_category'),
    ('OTHER', 'outbound_category');