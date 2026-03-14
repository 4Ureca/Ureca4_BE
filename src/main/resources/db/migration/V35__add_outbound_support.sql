-- ============================================================
-- V34: 아웃바운드 분석 지원
-- 1. analysis_code 테이블 신규 생성
-- 2. retention_analysis 에 아웃바운드 분석용 컬럼 추가
-- ============================================================


-- ============================================================
-- 1. analysis_code 테이블 생성
-- ============================================================
CREATE TABLE analysis_code (
                               id             BIGINT      NOT NULL AUTO_INCREMENT          COMMENT '식별자',
                               code_name      VARCHAR(20) NOT NULL                         COMMENT '코드명',
                               classification ENUM(
        'complaint_category',
        'defense_category',
        'outbound_category'
    )              NOT NULL                                     COMMENT '분류 구분',

                               PRIMARY KEY (id),
                               UNIQUE KEY uq_analysis_code_name (code_name),               -- FK 대상 단독 UNIQUE
                               UNIQUE KEY uq_analysis_code_full (code_name, classification) -- 분류 내 중복 방지
) COMMENT = '해지방어 분석 코드 마스터';


-- ============================================================
-- 2. analysis_code 기초 데이터 삽입
-- ============================================================

-- complaint_category (해지 사유)
INSERT INTO analysis_code (code_name, classification) VALUES
                                                          ('COST_HIGH',    'complaint_category'),   -- 비용 부담: 요금 과다
                                                          ('COST_PENALTY', 'complaint_category'),   -- 비용 부담: 위약금 문의
                                                          ('COMP_BENEFIT', 'complaint_category'),   -- 경쟁사 유혹: 타사 혜택 및 번호이동
                                                          ('QUAL_SPEED',   'complaint_category'),   -- 서비스 불만: 품질 및 속도
                                                          ('QUAL_TECH',    'complaint_category'),   -- 서비스 불만: 기술 문의 및 장애
                                                          ('ENV_MOVE',     'complaint_category'),   -- 환경 변화: 이사 및 이전
                                                          ('ENV_UNUSED',   'complaint_category'),   -- 환경 변화: 서비스 불필요 및 미사용
                                                          ('ETC_BILLING',  'complaint_category');   -- 기타: 부가서비스 및 청구 문제

-- defense_category (방어 조치)
INSERT INTO analysis_code (code_name, classification) VALUES
                                                          ('BNFT_DISCOUNT',   'defense_category'),  -- 금전적 혜택: 직접 할인 (재약정, 결합 등)
                                                          ('BNFT_GIFT',       'defense_category'),  -- 금전적 혜택: 일시금 및 바우처 (상품권 등)
                                                          ('OPT_DOWNGRADE',   'defense_category'),  -- 상품 최적화: 요금제 하향 제안
                                                          ('PHYS_RELOCATION', 'defense_category'),  -- 물리적 조치: 이전 설치 지원 및 혜택
                                                          ('PHYS_TECH_CHECK', 'defense_category'),  -- 물리적 조치: 기술 점검 (원격, 출동)
                                                          ('ADM_CLOSE_FAIL',  'defense_category'),  -- 행정/상담: 해지 방어 실패 (접수 진행)
                                                          ('ADM_GUIDE',       'defense_category');  -- 행정/상담: 단순 정보 안내 (위약금, 절감액 등)

-- outbound_category (아웃바운드 거절 사유)
INSERT INTO analysis_code (code_name, classification) VALUES
                                                          ('COST',         'outbound_category'),    -- 가격/비용 부담
                                                          ('NO_NEED',      'outbound_category'),    -- 필요 없음 / 관심 없음
                                                          ('SWITCH',       'outbound_category'),    -- 타사 전환 예정
                                                          ('CONSIDER',     'outbound_category'),    -- 추후 검토 (보류)
                                                          ('DISSATISFIED', 'outbound_category'),    -- 서비스 불만
                                                          ('OTHER',        'outbound_category');    -- 기타


-- ============================================================
-- 3. retention_analysis 컬럼 추가
-- ============================================================
ALTER TABLE retention_analysis
    ADD COLUMN outbound_call_result ENUM('CONVERTED', 'REJECTED') NULL
        COMMENT '아웃바운드 상담 결과: 전환성공 / 거절',

    ADD COLUMN outbound_report      JSON                          NULL
        COMMENT 'AI 분석 결과 (JSON)',

    ADD COLUMN complaint_category   VARCHAR(20)                   NULL
        COMMENT '해지 사유 코드 (FK → analysis_code.code_name)',

    ADD COLUMN defense_category     VARCHAR(20)                   NULL
        COMMENT '방어 조치 코드 (FK → analysis_code.code_name)',

    ADD COLUMN outbound_category    VARCHAR(20)                   NULL
        COMMENT '아웃바운드 거절 사유 코드 (FK → analysis_code.code_name)';


-- ============================================================
-- 4. FK 제약 추가
-- ============================================================
ALTER TABLE retention_analysis
    ADD CONSTRAINT fk_retention_complaint_category
        FOREIGN KEY (complaint_category)
            REFERENCES analysis_code (code_name)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    ADD CONSTRAINT fk_retention_defense_category
        FOREIGN KEY (defense_category)
        REFERENCES analysis_code (code_name)
        ON UPDATE CASCADE
           ON DELETE RESTRICT,

    ADD CONSTRAINT fk_retention_outbound_category
        FOREIGN KEY (outbound_category)
        REFERENCES analysis_code (code_name)
        ON UPDATE CASCADE
           ON DELETE RESTRICT;


-- ============================================================
-- 5. 인덱스 추가
-- (FK 컬럼은 MySQL이 자동 인덱스 생성 — 별도 추가 불필요)
-- ============================================================
CREATE INDEX idx_retention_outbound_call_result ON retention_analysis (outbound_call_result);