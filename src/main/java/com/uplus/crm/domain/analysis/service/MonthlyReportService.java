package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.ChurnDefenseResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.entity.AnalysisCode;
import com.uplus.crm.domain.analysis.repository.AnalysisCodeRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION = "monthly_report_snapshot";

    private final AnalysisCodeRepository analysisCodeRepository;

    /**
     * 월별 고객 특이사항 조회
     *
     * @param date 해당 월에 포함되는 아무 날짜 (예: 2025-01-15 → 2025년 1월 스냅샷)
     * @return CustomerRiskResponse (surgeAlerts 없이 통계만)
     */
    public CustomerRiskResponse getMonthlyCustomerRisk(LocalDate date) {
        Document snapshot = findSnapshotContaining(date);
        if (snapshot == null) {
            log.info("[MonthlyReport] {} 포함 월별 스냅샷 없음", date);
            return null;
        }

        Document risk = snapshot.get("customerRiskAnalysis", Document.class);
        if (risk == null) {
            log.info("[MonthlyReport] {} 스냅샷에 customerRiskAnalysis 필드 없음", date);
            return null;
        }

        int fraudSuspect = risk.getInteger("fraudSuspect", 0);
        int maliciousComplaint = risk.getInteger("maliciousComplaint", 0);
        int policyAbuse = risk.getInteger("policyAbuse", 0);
        int excessiveCompensation = risk.getInteger("excessiveCompensation", 0);
        int repeatedComplaint = risk.getInteger("repeatedComplaint", 0);
        int phishingVictim = risk.getInteger("phishingVictim", 0);
        int churnRisk = risk.getInteger("churnRisk", 0);
        int totalRiskCount = risk.getInteger("totalRiskCount",
                fraudSuspect + maliciousComplaint + policyAbuse
                        + excessiveCompensation + repeatedComplaint + phishingVictim + churnRisk);

        Date startDate = snapshot.get("startAt", Date.class);
        Date endDate = snapshot.get("endAt", Date.class);
        LocalDateTime startAt = startDate != null
                ? startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;
        LocalDateTime endAt = endDate != null
                ? endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null;

        return CustomerRiskResponse.builder()
                .startAt(startAt)
                .endAt(endAt)
                .fraudSuspect(fraudSuspect)
                .maliciousComplaint(maliciousComplaint)
                .policyAbuse(policyAbuse)
                .excessiveCompensation(excessiveCompensation)
                .repeatedComplaint(repeatedComplaint)
                .phishingVictim(phishingVictim)
                .churnRisk(churnRisk)
                .totalRiskCount(totalRiskCount)
                .surgeAlerts(null) // 월별은 급증 경고 없음
                .build();
    }

    /**
     * 월별 키워드 분석 조회
     *
     * @param date 해당 월에 포함되는 아무 날짜
     * @return KeywordAnalysisResponse (topKeywords, longTermTopKeywords, byCustomerType)
     */
    public KeywordAnalysisResponse getMonthlyKeywordAnalysis(LocalDate date) {
        Document snapshot = findSnapshotContaining(date);
        if (snapshot == null) {
            log.info("[MonthlyReport] {} 포함 월별 스냅샷 없음 (키워드)", date);
            return null;
        }

        KeywordAnalysisResponse response = KeywordAnalysisResponse.from(snapshot);
        if (response == null) {
            log.info("[MonthlyReport] {} 스냅샷에 keywordSummary 필드 없음", date);
        }
        return response;
    }

    /**
     * 월별 해지방어 패턴 분석 조회
     *
     * @param date 해당 월에 포함되는 아무 날짜
     * @return ChurnDefenseResponse (totalAttempts, successRate, complaintReasons, byCustomerType, byAction)
     */
    public ChurnDefenseResponse getMonthlyChurnDefenseAnalysis(LocalDate date) {

        Document snapshot = findSnapshotContaining(date);
        if (snapshot == null) return null;

        ChurnDefenseResponse response = ChurnDefenseResponse.from(snapshot);

        if (response != null) {
            List<AnalysisCode> allCodes = analysisCodeRepository.findAll();

            // 1. 불만 사유용 맵 (displayName 사용)
            Map<String, String> complaintMap = allCodes.stream()
                .filter(c -> "complaint_category".equals(c.getClassification()))
                .collect(Collectors.toMap(AnalysisCode::getCodeName, AnalysisCode::getDisplayName, (a, b) -> b));

            // 2. 방어 액션용 맵 (displayName 사용)
            Map<String, String> defenseMap = allCodes.stream()
                .filter(c -> "defense_category".equals(c.getClassification()))
                .collect(Collectors.toMap(AnalysisCode::getCodeName, AnalysisCode::getDisplayName, (a, b) -> b));

            // --- DTO 치환 로직 ---

            // A. 불만 사유
            if (response.getComplaintReasons() != null) {
                response.getComplaintReasons().forEach(dto ->
                    dto.setReason(complaintMap.getOrDefault(dto.getReason(), dto.getReason())));
            }

            // B. 고객 유형별 주요 불만 사유
            if (response.getByCustomerType() != null) {
                response.getByCustomerType().forEach(dto ->
                    dto.setMainComplaintReason(complaintMap.getOrDefault(dto.getMainComplaintReason(), dto.getMainComplaintReason())));
            }

            // C. 방어 액션
            if (response.getByAction() != null) {
                response.getByAction().forEach(actionDto -> {
                    // 방어 액션명 매핑
                    actionDto.setAction(defenseMap.getOrDefault(actionDto.getAction(), actionDto.getAction()));

                    // 액션 하위 상세 사유 매핑
                    if (actionDto.getByReason() != null) {
                        actionDto.getByReason().forEach(reasonDto ->
                            reasonDto.setReason(complaintMap.getOrDefault(reasonDto.getReason(), reasonDto.getReason())));
                    }
                });
            }
        }
        return response;
    }

    /**
     * date가 포함되는 스냅샷 조회 (startAt <= date <= endAt)
     */
    private Document findSnapshotContaining(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
        ).limit(1);
        return mongoTemplate.findOne(query, Document.class, COLLECTION);
    }
}
