-- retention_analysis: 아웃바운드 결과서 컬럼 타입을 TEXT로 변경
-- 기존 데이터 타입이 VARCHAR였다면, 더 긴 텍스트 저장을 위해 TEXT 타입으로 확장합니다.
ALTER TABLE retention_analysis
    MODIFY COLUMN outbound_report TEXT NULL COMMENT '아웃바운드 상담 결과서 내용';