-- 1. analysis_code 테이블에 description 컬럼 추가
ALTER TABLE analysis_code
    ADD COLUMN description TEXT NULL COMMENT '코드 의미 및 비즈니스 역할 설명' AFTER classification;

-- 2. complaint_category : 인바운드 고객 불만·문의 유형 설명 업데이트
UPDATE analysis_code SET description = '월정료·데이터 요금이 과도하다는 비용 부담으로 인한 해지 의향' WHERE code_name = 'COST_HIGH' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '약정 해지 시 발생하는 위약금 금액 확인 및 부담으로 인한 해지 문의' WHERE code_name = 'COST_PENALTY' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '타사의 요금·혜택 우위 또는 번호이동 프로모션 유혹으로 인한 이탈 의향' WHERE code_name = 'COMP_BENEFIT' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '인터넷·통신 속도 저하 및 품질 불량에 대한 서비스 불만' WHERE code_name = 'QUAL_SPEED' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '기술적 장애·서비스 불통·단말 이슈 등 기술 문의 및 장애 불만' WHERE code_name = 'QUAL_TECH' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '이사·이전으로 인한 서비스 주소 변경 또는 설치 필요성' WHERE code_name = 'ENV_MOVE' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '실사용 없음 또는 서비스 불필요 상태로 인한 해지 의향' WHERE code_name = 'ENV_UNUSED' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '부가서비스 청구 오류, 내역 불명확, 미인지 요금 관련 문의' WHERE code_name = 'ETC_BILLING' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '서비스 완전 불통·장기 장애로 인한 즉각적 해소 요구' WHERE code_name = 'SVC_FAULT' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '청구서 항목 확인, 요금 계산 방식, 납부 내역 조회 목적의 문의' WHERE code_name = 'FEE_INQUIRY' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '단말기 노후화·파손·기종 교체 희망으로 인한 변경 요청' WHERE code_name = 'DEVICE_CHANGE' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '불필요 부가서비스 해지 또는 구성 변경 요청' WHERE code_name = 'ADDON_CHG' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '약정 기간 만료 이후 갱신 없이 해지를 희망하는 의향' WHERE code_name = 'CONTRACT_END' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '동일 고객 명의의 중복 회선·서비스 정리를 목적으로 한 해지 의향' WHERE code_name = 'DUPLICATE_SVC' AND classification = 'complaint_category';
UPDATE analysis_code SET description = '위 분류 코드에 해당하지 않는 인바운드 기타 불만·문의 사유' WHERE code_name = 'OTHER' AND classification = 'complaint_category';

-- 3. defense_category : 상담사 해지방어 조치 유형 설명 업데이트
UPDATE analysis_code SET description = '요금 할인 쿠폰, 월정료 감면, 비용 절감 혜택 제공을 통한 유지 유도' WHERE code_name = 'BNFT_DISCOUNT' AND classification = 'defense_category';
UPDATE analysis_code SET description = '사은품·경품·기기 지원 등 물질적 혜택 제공을 통한 유지 유도' WHERE code_name = 'BNFT_GIFT' AND classification = 'defense_category';
UPDATE analysis_code SET description = '고객 사용 패턴에 맞는 하위 요금제·옵션으로의 변경을 통한 비용 절감 제안' WHERE code_name = 'OPT_DOWNGRADE' AND classification = 'defense_category';
UPDATE analysis_code SET description = '이사지 서비스 커버리지 확인 및 이전 설치 지원 안내' WHERE code_name = 'PHYS_RELOCATION' AND classification = 'defense_category';
UPDATE analysis_code SET description = '품질·장애 문제 해소를 위한 기술 점검 및 현장 출동 서비스 제공' WHERE code_name = 'PHYS_TECH_CHECK' AND classification = 'defense_category';
UPDATE analysis_code SET description = '방어 시도에도 고객이 해지·이탈을 최종 결정한 경우의 처리' WHERE code_name = 'ADM_CLOSE_FAIL' AND classification = 'defense_category';
UPDATE analysis_code SET description = '충분한 안내·상담 후 고객이 자발적으로 유지를 결정한 경우' WHERE code_name = 'ADM_GUIDE' AND classification = 'defense_category';
UPDATE analysis_code SET description = '재약정 시 제공되는 혜택·할인 조건 제시를 통한 장기 유지 유도' WHERE code_name = 'CONTRACT_RENEW' AND classification = 'defense_category';
UPDATE analysis_code SET description = 'U+ 멤버십 포인트·마일리지 지급을 통한 충성 고객 유지' WHERE code_name = 'LOYALTY_POINT' AND classification = 'defense_category';
UPDATE analysis_code SET description = '고객의 현재 상황·사용량에 최적화된 요금제 변경 제안' WHERE code_name = 'PLAN_CHANGE' AND classification = 'defense_category';
UPDATE analysis_code SET description = '위 분류 코드에 해당하지 않는 기타 해지방어 조치' WHERE code_name = 'OTHER' AND classification = 'defense_category';

-- 4. outbound_category : 아웃바운드 고객 거절 사유 설명 업데이트
UPDATE analysis_code SET description = '아웃바운드 제안에 대해 요금·비용 부담을 이유로 거절한 경우' WHERE code_name = 'COST' AND classification = 'outbound_category';
UPDATE analysis_code SET description = '이사·이전·서비스 불필요 등 환경 변화를 이유로 거절한 경우' WHERE code_name = 'NO_NEED' AND classification = 'outbound_category';
UPDATE analysis_code SET description = '타사 전환 의사가 확고하여 유지 제안을 거절한 경우' WHERE code_name = 'SWITCH' AND classification = 'outbound_category';
UPDATE analysis_code SET description = '즉각 결정 없이 추가 검토 또는 보류 의사를 밝히며 거절한 경우' WHERE code_name = 'CONSIDER' AND classification = 'outbound_category';
UPDATE analysis_code SET description = '기존 서비스 품질·기술적 불만이 해소되지 않아 거절한 경우' WHERE code_name = 'DISSATISFIED' AND classification = 'outbound_category';
UPDATE analysis_code SET description = '위 분류 코드에 해당하지 않는 기타 사유로 아웃바운드 제안을 거절한 경우' WHERE code_name = 'OTHER' AND classification = 'outbound_category';