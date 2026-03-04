-- ============================================================
-- Migration: V22__Add_summary_column_and_create_event_table
-- 작성일: 2026-03-03
-- 대상 테이블: retention_analysis, result_event_status
-- 작성자: 조승혁
-- 변경 목적: 1. 기존 분석 테이블에 원문 요약(raw_summary) 컬럼 추가
--            2. 상담 결과 이벤트 상태 관리 테이블 신규 생성
-- ============================================================

-- 1. 기존 retention_analysis 테이블 수정 (원문 요약 컬럼 추가)
-- 이미 존재하는 테이블이므로 ALTER 문을 사용하여 컬럼만 추가합니다.
ALTER TABLE retention_analysis
    ADD COLUMN `raw_summary` TEXT NOT NULL 
    COMMENT '원문 요약 (AI 생성)' 
    AFTER `defense_actions`;


-- 2. 신규 result_event_status 테이블 생성
-- 상담 결과 추출 요청 및 배치 작업의 상태를 관리합니다.
CREATE TABLE `result_event_status` (
    `result_event_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `consult_id` BIGINT NOT NULL COMMENT '상담 결과서 ID (FK)',
    `category_code` VARCHAR(20) NOT NULL COMMENT '상담 카테고리 코드',
    `status` VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '처리 상태 (VARCHAR로 자바 Enum 매핑)',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    `fail_reason` TEXT COMMENT '실패 원인',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이벤트 생성 시간',
    `started_at` DATETIME NULL COMMENT '작업 시작 시간',
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정된 시간',
    PRIMARY KEY (`result_event_id`),
    -- 최신 상태 조회를 위한 복합 인덱스
    INDEX `idx_result_event_status_consult_created` (`consult_id`, `created_at` DESC),
    -- 스케줄러 필터링 성능을 위한 상태 인덱스
    INDEX `idx_result_event_status_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담결과 요약 요청 이벤트 관리';