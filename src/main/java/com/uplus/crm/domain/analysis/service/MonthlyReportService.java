package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
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
