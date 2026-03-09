package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.analysis.dto.CategorySummaryResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse.ChangeDetail;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse.RiskSnapshot;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse.SurgeAlert;
import com.uplus.crm.domain.analysis.dto.KeywordRankingResponse;
import com.uplus.crm.domain.analysis.dto.TimeSlotTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION = "daily_report_snapshot";
    private static final double SURGE_THRESHOLD = 50.0;
    private static final int SURGE_TYPE_MULTIPLIER = 2;

    public CustomerRiskResponse getCustomerRisk(LocalDate targetDate) {
        LocalDateTime startAt = targetDate.atStartOfDay();

        Document todayDoc = findSnapshot(startAt);
        if (todayDoc == null) {
            log.info("[DailyReport] {} 스냅샷 없음 (배치 미실행)", targetDate);
            return null;
        }

        Document risk = extractRiskOrThrow(todayDoc, targetDate);

        int fraudSuspect = risk.getInteger("fraudSuspect", 0);
        int maliciousComplaint = risk.getInteger("maliciousComplaint", 0);
        int policyAbuse = risk.getInteger("policyAbuse", 0);
        int excessiveCompensation = risk.getInteger("excessiveCompensation", 0);
        int repeatedComplaint = risk.getInteger("repeatedComplaint", 0);
        int phishingVictim = risk.getInteger("phishingVictim", 0);
        int churnRisk = risk.getInteger("churnRisk", 0);

        int totalRiskCount = fraudSuspect + maliciousComplaint + policyAbuse
                + excessiveCompensation + repeatedComplaint + phishingVictim + churnRisk;

        LocalDateTime previousStartAt = targetDate.minusDays(1).atStartOfDay();
        Document prevDoc = findSnapshot(previousStartAt);
        SurgeAlert surgeAlert = calculateSurgeAlert(risk, prevDoc, totalRiskCount);

        return CustomerRiskResponse.builder()
                .startAt(startAt)
                .endAt(targetDate.atTime(23, 59, 59))
                .fraudSuspect(fraudSuspect)
                .maliciousComplaint(maliciousComplaint)
                .policyAbuse(policyAbuse)
                .excessiveCompensation(excessiveCompensation)
                .repeatedComplaint(repeatedComplaint)
                .phishingVictim(phishingVictim)
                .churnRisk(churnRisk)
                .totalRiskCount(totalRiskCount)
                .surgeAlerts(surgeAlert)
                .build();
    }

    // ==================== 고객 특이사항 기간 비교 ====================

    public CustomerRiskCompareResponse compareCustomerRisk(LocalDate baseDate, LocalDate compareDate) {
        Document baseDoc = findSnapshot(baseDate.atStartOfDay());
        Document compareDoc = findSnapshot(compareDate.atStartOfDay());

        if (baseDoc == null || compareDoc == null) {
            log.info("[DailyReport] 비교 스냅샷 없음 - base={} compare={}", baseDate, compareDate);
            return null;
        }

        Document baseRisk = extractRiskOrThrow(baseDoc, baseDate);
        Document compareRisk = extractRiskOrThrow(compareDoc, compareDate);

        RiskSnapshot baseSnapshot = toRiskSnapshot(baseRisk);
        RiskSnapshot compareSnapshot = toRiskSnapshot(compareRisk);

        String[] riskFields = {
                "fraudSuspect", "maliciousComplaint", "policyAbuse",
                "excessiveCompensation", "repeatedComplaint", "phishingVictim", "churnRisk"
        };
        String[] surgeCodes = {
                "FRAUD", "ABUSE", "POLICY", "COMP", "REPEAT", "PHISHING", "CHURN"
        };

        Map<String, ChangeDetail> changes = new LinkedHashMap<>();
        List<String> surgeTypes = new ArrayList<>();

        for (int i = 0; i < riskFields.length; i++) {
            int baseVal = baseRisk.getInteger(riskFields[i], 0);
            int compVal = compareRisk.getInteger(riskFields[i], 0);
            int diff = baseVal - compVal;
            double rate = calcChangeRate(baseVal, compVal);

            changes.put(riskFields[i], ChangeDetail.builder()
                    .diff(diff)
                    .changeRate(rate)
                    .build());

            if (compVal > 0 && baseVal >= compVal * SURGE_TYPE_MULTIPLIER) {
                surgeTypes.add(surgeCodes[i]);
            } else if (compVal == 0 && baseVal >= 3) {
                surgeTypes.add(surgeCodes[i]);
            }
        }

        int totalDiff = baseSnapshot.getTotalRiskCount() - compareSnapshot.getTotalRiskCount();
        double totalRate = calcChangeRate(baseSnapshot.getTotalRiskCount(), compareSnapshot.getTotalRiskCount());
        changes.put("totalRiskCount", ChangeDetail.builder()
                .diff(totalDiff)
                .changeRate(totalRate)
                .build());

        boolean surgeDetected = totalRate >= SURGE_THRESHOLD || !surgeTypes.isEmpty();

        return CustomerRiskCompareResponse.builder()
                .baseDate(baseDate)
                .compareDate(compareDate)
                .base(baseSnapshot)
                .compare(compareSnapshot)
                .changes(changes)
                .surgeDetected(surgeDetected)
                .surgeTypes(surgeTypes)
                .build();
    }


    // ==================== 시간대별 트렌드 / 카테고리 / 키워드 ====================

    public Optional<TimeSlotTrendResponse> getTimeSlotTrend(LocalDate date, String slot) {
        return Optional.ofNullable(findSnapshot(date.atStartOfDay()))
                .map(doc -> TimeSlotTrendResponse.from(date, doc, slot));
    }

    public Optional<CategorySummaryResponse> getCategorySummary(LocalDate date, String slot) {
        return Optional.ofNullable(findSnapshot(date.atStartOfDay()))
                .map(doc -> CategorySummaryResponse.from(date, doc, slot));
    }

    public Optional<KeywordRankingResponse> getKeywordRanking(LocalDate date, String slot) {
        return Optional.ofNullable(findSnapshot(date.atStartOfDay()))
                .map(doc -> KeywordRankingResponse.from(date, doc, slot));
    }

    private Document findSnapshot(LocalDateTime startAt) {
        Query query = new Query(Criteria.where("startAt").is(startAt));
        return mongoTemplate.findOne(query, Document.class, COLLECTION);
    }

    private SurgeAlert calculateSurgeAlert(Document todayRisk, Document prevDoc, int todayTotal) {
        if (prevDoc == null) return null;
        Document prevRisk = prevDoc.get("customerRiskAnalysis", Document.class);
        if (prevRisk == null) return null;

        int prevTotal = prevRisk.getInteger("fraudSuspect", 0)
                + prevRisk.getInteger("maliciousComplaint", 0)
                + prevRisk.getInteger("policyAbuse", 0)
                + prevRisk.getInteger("excessiveCompensation", 0)
                + prevRisk.getInteger("repeatedComplaint", 0)
                + prevRisk.getInteger("phishingVictim", 0)
                + prevRisk.getInteger("churnRisk", 0);

        double changeRate = (prevTotal == 0)
                ? (todayTotal > 0 ? 100.0 : 0.0)
                : ((double)(todayTotal - prevTotal) / prevTotal) * 100.0;
        changeRate = Math.round(changeRate * 10.0) / 10.0;

        List<String> surgeTypes = new ArrayList<>();
        checkTypeSurge(surgeTypes, "FRAUD", todayRisk, prevRisk, "fraudSuspect");
        checkTypeSurge(surgeTypes, "ABUSE", todayRisk, prevRisk, "maliciousComplaint");
        checkTypeSurge(surgeTypes, "POLICY", todayRisk, prevRisk, "policyAbuse");
        checkTypeSurge(surgeTypes, "COMP", todayRisk, prevRisk, "excessiveCompensation");
        checkTypeSurge(surgeTypes, "REPEAT", todayRisk, prevRisk, "repeatedComplaint");
        checkTypeSurge(surgeTypes, "PHISHING", todayRisk, prevRisk, "phishingVictim");
        checkTypeSurge(surgeTypes, "CHURN", todayRisk, prevRisk, "churnRisk");

        boolean surgeDetected = changeRate >= SURGE_THRESHOLD || !surgeTypes.isEmpty();

        return SurgeAlert.builder()
                .previousTotalRiskCount(prevTotal)
                .changeRate(changeRate)
                .surgeDetected(surgeDetected)
                .surgeTypes(surgeTypes)
                .build();
    }

    private void checkTypeSurge(List<String> surgeTypes, String typeCode,
                                Document todayRisk, Document prevRisk, String field) {
        int today = todayRisk.getInteger(field, 0);
        int prev = prevRisk.getInteger(field, 0);
        if (prev > 0 && today >= prev * SURGE_TYPE_MULTIPLIER) {
            surgeTypes.add(typeCode);
        } else if (prev == 0 && today >= 3) {
            surgeTypes.add(typeCode);
        }
    }

    private Document extractRiskOrThrow(Document snapshot, LocalDate date) {
        Document risk = snapshot.get("customerRiskAnalysis", Document.class);
        if (risk == null) {
            throw new BusinessException(ErrorCode.DATA_INTEGRITY_ERROR,
                    date + " 스냅샷에 customerRiskAnalysis 필드가 없습니다");
        }
        return risk;
    }

    private RiskSnapshot toRiskSnapshot(Document risk) {
        int fraud = risk.getInteger("fraudSuspect", 0);
        int malicious = risk.getInteger("maliciousComplaint", 0);
        int policy = risk.getInteger("policyAbuse", 0);
        int excessive = risk.getInteger("excessiveCompensation", 0);
        int repeated = risk.getInteger("repeatedComplaint", 0);
        int phishing = risk.getInteger("phishingVictim", 0);
        int churn = risk.getInteger("churnRisk", 0);
        int total = fraud + malicious + policy + excessive + repeated + phishing + churn;

        return RiskSnapshot.builder()
                .fraudSuspect(fraud)
                .maliciousComplaint(malicious)
                .policyAbuse(policy)
                .excessiveCompensation(excessive)
                .repeatedComplaint(repeated)
                .phishingVictim(phishing)
                .churnRisk(churn)
                .totalRiskCount(total)
                .build();
    }

    private double calcChangeRate(int current, int previous) {
        double rate = (previous == 0)
                ? (current > 0 ? 100.0 : 0.0)
                : ((double)(current - previous) / previous) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }

}