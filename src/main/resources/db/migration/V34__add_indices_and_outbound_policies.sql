-- 1. 성능 최적화를 위한 인덱스 생성
CREATE INDEX idx_evaluation_created_at ON consultation_evaluations (created_at DESC);
CREATE INDEX idx_evaluation_score ON consultation_evaluations (score DESC);
CREATE INDEX idx_evaluation_status_score ON consultation_evaluations (selection_status, score DESC);

-- 2. 아웃바운드 카테고리 정책 추가
INSERT INTO consultation_category_policy
    (category_code, large_category, medium_category, small_category, is_active, sort_order)
VALUES
    ('M_OTB_01', '아웃바운드', '재약정 권유',       '약정 만료 고객 재약정 제안',                          1, 701),
    ('M_OTB_02', '아웃바운드', '요금제 업셀링',     '상위 요금제 전환 제안',                                1, 702),
    ('M_OTB_03', '아웃바운드', '해지방어 사후관리', '해지 철회 고객 사후 만족도 확인',                      1, 703),
    ('M_OTB_04', '아웃바운드', '연체/납부 안내',    '미납 요금 납부 독려 및 분납 안내',                     1, 704),
    ('M_OTB_05', '아웃바운드', '윈백(Win-back)',    '이탈 고객 복귀 유도',                                  1, 705),
    ('M_OTB_06', '아웃바운드', '해피콜/만족도조사', '설치/개통 후 만족도 확인 및 추가 니즈 발굴',           1, 706);