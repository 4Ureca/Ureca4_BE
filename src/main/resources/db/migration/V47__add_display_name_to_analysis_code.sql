-- analysis_code 테이블에 display_name 컬럼 추가
ALTER TABLE analysis_code
    ADD COLUMN display_name VARCHAR(30) NULL COMMENT '화면 표시용 짧은 한글명' AFTER code_name;

-- complaint_category
UPDATE analysis_code SET display_name = '요금 부담' WHERE code_name = 'COST_HIGH' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '위약금 부담' WHERE code_name = 'COST_PENALTY' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '타사 혜택' WHERE code_name = 'COMP_BENEFIT' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '속도 불만' WHERE code_name = 'QUAL_SPEED' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '기술 장애' WHERE code_name = 'QUAL_TECH' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '이사·이전' WHERE code_name = 'ENV_MOVE' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '미사용' WHERE code_name = 'ENV_UNUSED' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '청구 오류' WHERE code_name = 'ETC_BILLING' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '서비스 불통' WHERE code_name = 'SVC_FAULT' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '요금 조회' WHERE code_name = 'FEE_INQUIRY' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '기기 변경' WHERE code_name = 'DEVICE_CHANGE' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '부가서비스 변경' WHERE code_name = 'ADDON_CHG' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '약정 만료' WHERE code_name = 'CONTRACT_END' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '중복 회선' WHERE code_name = 'DUPLICATE_SVC' AND classification = 'complaint_category';
UPDATE analysis_code SET display_name = '기타' WHERE code_name = 'OTHER' AND classification = 'complaint_category';

-- defense_category
UPDATE analysis_code SET display_name = '요금 할인' WHERE code_name = 'BNFT_DISCOUNT' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '사은품 제공' WHERE code_name = 'BNFT_GIFT' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '요금제 다운' WHERE code_name = 'OPT_DOWNGRADE' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '이전 설치' WHERE code_name = 'PHYS_RELOCATION' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '기술 점검' WHERE code_name = 'PHYS_TECH_CHECK' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '방어 실패' WHERE code_name = 'ADM_CLOSE_FAIL' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '안내 유지' WHERE code_name = 'ADM_GUIDE' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '재약정' WHERE code_name = 'CONTRACT_RENEW' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '포인트 지급' WHERE code_name = 'LOYALTY_POINT' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '요금제 변경' WHERE code_name = 'PLAN_CHANGE' AND classification = 'defense_category';
UPDATE analysis_code SET display_name = '기타' WHERE code_name = 'OTHER' AND classification = 'defense_category';

-- outbound_category
UPDATE analysis_code SET display_name = '비용 부담' WHERE code_name = 'COST' AND classification = 'outbound_category';
UPDATE analysis_code SET display_name = '필요 없음' WHERE code_name = 'NO_NEED' AND classification = 'outbound_category';
UPDATE analysis_code SET display_name = '타사 전환' WHERE code_name = 'SWITCH' AND classification = 'outbound_category';
UPDATE analysis_code SET display_name = '검토 중' WHERE code_name = 'CONSIDER' AND classification = 'outbound_category';
UPDATE analysis_code SET display_name = '서비스 불만' WHERE code_name = 'DISSATISFIED' AND classification = 'outbound_category';
UPDATE analysis_code SET display_name = '기타' WHERE code_name = 'OTHER' AND classification = 'outbound_category';
