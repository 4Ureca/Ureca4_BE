-- V36__filter_data_updates.sql

DELETE FROM filter_custom;
DELETE FROM filter;
ALTER TABLE filter AUTO_INCREMENT = 1;

INSERT INTO filter (filter_key, filter_name) VALUES
('keyword',          '통합 검색 키워드'),
('agentId',          '상담사 ID'),
('agentName',        '상담사 이름'),
('channel',          '상담 채널'),
('categoryCode',     '상담 분류 코드'),
('categoryLarge',    '상담 대분류'),
('categoryMedium',   '상담 중분류'),
('categorySmall',    '상담 소분류'),
('customerId',       '고객 ID'),
('customerName',     '고객 이름'),
('customerPhone',    '고객 전화번호'),
('grade',            '고객 등급'),
('gender',           '고객 성별'),
('intent',           '해지 의사 여부'),
('defenseAttempted', '해지 방어 시도 여부'),
('defenseSuccess',   '해지 방어 성공 여부'),
('complaintCategory', '해지 사유'),
('defenseCategory',   '방어 조치'),
('outboundCategory',  '아웃바운드 거절 사유'),
('outboundResult',    '아웃바운드 상담 결과'),
('riskType',         '리스크 유형'),
('riskLevel',        '리스크 레벨'),
('productCode',      '상품 코드'),
('fromDate',         '상담 시작일'),
('toDate',           '상담 종료일'),
('minDuration',      '최소 상담 시간'),
('maxDuration',      '최대 상담 시간'),
('page',             '페이지 번호'),
('size',             '페이지 크기'),
('sort',             '정렬 기준');