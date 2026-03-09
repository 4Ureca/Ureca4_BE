package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.CategorySummaryResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse.ChangeDetail;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse.RiskSnapshot;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse.SurgeAlert;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.KeywordRankingResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final MongoTemplate mongoTemplate;

    private static final String COLLECTION = "daily_report_snapshot";
    private static final String AGENT_COLLECTION = "daily_agent_report_snapshot";
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
        if (slot != null) {
            // 슬롯별 키워드: 성과 문서(startAt 기준)의 timeSlotTrend에서 조회
            return Optional.ofNullable(findSnapshot(date.atStartOfDay()))
                    .map(doc -> KeywordRankingResponse.from(date, doc, slot));
        }
        // 전체 키워드: KeywordRankTasklet이 저장한 키워드 문서(date 기준)에서 조회
        Document keywordDoc = findKeywordSnapshot(date);
        if (keywordDoc != null) {
            return Optional.of(KeywordRankingResponse.fromDaily(date, keywordDoc));
        }
        return Optional.empty();
    }

    /**
     * 일별 고객 유형별 키워드 조회
     *
     * daily_report_snapshot(키워드 문서, date 기준)의 byGradeCode에서 조회합니다.
     */
    public Optional<KeywordAnalysisResponse> getDailyCustomerTypeKeywords(LocalDate date) {
        Document keywordDoc = findKeywordSnapshot(date);
        if (keywordDoc == null) return Optional.empty();

        List<Document> gradeList = keywordDoc.getList("byGradeCode", Document.class);
        if (gradeList == null || gradeList.isEmpty()) return Optional.empty();

        List<KeywordAnalysisResponse.CustomerTypeKeyword> byCustomerType = gradeList.stream()
                .map(g -> {
                    List<Document> kwDocs = g.getList("keywords", Document.class);
                    List<String> keywords = (kwDocs != null)
                            ? kwDocs.stream()
                                .map(kw -> kw.getString("keyword"))
                                .collect(Collectors.toList())
                            : List.of();
                    return KeywordAnalysisResponse.CustomerTypeKeyword.builder()
                            .customerType(g.getString("gradeCode"))
                            .keywords(keywords)
                            .build();
                })
                .collect(Collectors.toList());

        return Optional.of(KeywordAnalysisResponse.builder()
                .startDate(date.toString())
                .endDate(date.toString())
                .byCustomerType(byCustomerType)
                .build());
    }

    // ==================== 일별 상담사 성과/순위 ====================

    /**
     * 일별 전체 성과 요약
     *
     * daily_agent_report_snapshot에서 해당 날짜의 전체 상담사 데이터를 집계합니다.
     */
    public Optional<PerformanceSummaryResponse> getDailyPerformanceSummary(LocalDate date) {
        List<Document> agentDocs = findAgentSnapshots(date);

        if (agentDocs.isEmpty()) {
            log.info("[DailyReport] {} 상담사 스냅샷 없음 (성과)", date);
            return Optional.empty();
        }

        int totalConsultCount = agentDocs.stream()
                .mapToInt(d -> getIntOrZero(d, "consultCount"))
                .sum();

        int agentCount = agentDocs.size();
        double avgConsultCountPerAgent = agentCount > 0
                ? Math.round((double) totalConsultCount / agentCount * 10.0) / 10.0
                : 0;

        double avgDurationMinutes = agentDocs.stream()
                .mapToDouble(d -> getDoubleOrZero(d, "avgDurationMinutes"))
                .average().orElse(0.0);
        avgDurationMinutes = Math.round(avgDurationMinutes * 10.0) / 10.0;

        double avgSatisfiedScore = agentDocs.stream()
                .mapToDouble(this::extractSatisfactionScore)
                .average().orElse(0.0);
        avgSatisfiedScore = Math.round(avgSatisfiedScore * 10.0) / 10.0;

        return Optional.of(PerformanceSummaryResponse.builder()
                .startDate(date.toString())
                .endDate(date.toString())
                .totalConsultCount(totalConsultCount)
                .avgConsultCountPerAgent(avgConsultCountPerAgent)
                .avgDurationMinutes(avgDurationMinutes)
                .avgSatisfiedScore(avgSatisfiedScore)
                .build());
    }

    /**
     * 일별 상담사 성과 순위 (TOP 10)
     *
     * daily_agent_report_snapshot에서 종합 점수 기반으로 정렬하여 상위 10명을 반환합니다.
     * 종합 점수: 처리건수(25%) + 소요시간(15%) + 응대품질(30%) + 고객만족도(30%)
     */
    public Optional<AgentRankingResponse> getDailyAgentRanking(LocalDate date) {
        List<Document> agentDocs = findAgentSnapshots(date);

        if (agentDocs.isEmpty()) {
            log.info("[DailyReport] {} 상담사 스냅샷 없음 (순위)", date);
            return Optional.empty();
        }

        // 상담사 이름 조회용 맵 구축
        Map<Long, String> agentNameMap = buildAgentNameMap(agentDocs);

        // 중간 계산용 구조체
        List<AgentScoreHolder> holders = agentDocs.stream()
                .map(doc -> {
                    long agentId = getLongOrZero(doc, "agentId");
                    int consultCount = getIntOrZero(doc, "consultCount");
                    double avgDuration = getDoubleOrZero(doc, "avgDurationMinutes");
                    double satisfaction = extractSatisfactionScore(doc);
                    double quality = extractQualityScore(doc);

                    return new AgentScoreHolder(agentId, consultCount, avgDuration, satisfaction, quality);
                })
                .collect(Collectors.toList());

        // min-max 정규화를 위한 통계값
        if (holders.size() > 1) {
            double maxConsult = holders.stream().mapToInt(h -> h.consultCount).max().orElse(1);
            double minConsult = holders.stream().mapToInt(h -> h.consultCount).min().orElse(0);
            double maxDuration = holders.stream().mapToDouble(h -> h.avgDuration).max().orElse(1);
            double minDuration = holders.stream().mapToDouble(h -> h.avgDuration).min().orElse(0);

            holders.forEach(h -> h.compositeScore = calculateCompositeScore(
                    h.consultCount, h.avgDuration, h.quality, h.satisfaction,
                    minConsult, maxConsult, minDuration, maxDuration));

            holders.sort(Comparator.comparingDouble((AgentScoreHolder h) -> h.compositeScore).reversed());
        } else if (holders.size() == 1) {
            holders.get(0).compositeScore = 1.0;
        }

        int limit = Math.min(holders.size(), 10);
        List<AgentRankingResponse.AgentPerformance> agents = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            AgentScoreHolder h = holders.get(i);
            agents.add(AgentRankingResponse.AgentPerformance.builder()
                    .rank(i + 1)
                    .agentId(h.agentId)
                    .agentName(agentNameMap.get(h.agentId))
                    .consultCount(h.consultCount)
                    .avgDurationMinutes(Math.round(h.avgDuration * 10.0) / 10.0)
                    .avgSatisfiedScore(Math.round(h.satisfaction * 10.0) / 10.0)
                    .qualityScore(Math.round(h.quality * 10.0) / 10.0)
                    .build());
        }

        return Optional.of(AgentRankingResponse.builder()
                .startDate(date.toString())
                .endDate(date.toString())
                .agents(agents)
                .build());
    }

    private List<Document> findAgentSnapshots(LocalDate date) {
        LocalDateTime startAt = date.atStartOfDay();
        Query query = new Query(Criteria.where("startAt").is(startAt));
        return mongoTemplate.find(query, Document.class, AGENT_COLLECTION);
    }

    private double extractSatisfactionScore(Document doc) {
        Document csAnalysis = doc.get("customerSatisfactionAnalysis", Document.class);
        if (csAnalysis != null) {
            Object score = csAnalysis.get("satisfactionScore");
            return score instanceof Number ? ((Number) score).doubleValue() : 0.0;
        }
        return 0.0;
    }

    private double extractQualityScore(Document doc) {
        Document qa = doc.get("qualityAnalysis", Document.class);
        if (qa != null) {
            Object score = qa.get("totalScore");
            return score instanceof Number ? ((Number) score).doubleValue() : 0.0;
        }
        return 0.0;
    }

    private Map<Long, String> buildAgentNameMap(List<Document> agentDocs) {
        Map<Long, String> nameMap = new LinkedHashMap<>();
        for (Document doc : agentDocs) {
            long agentId = getLongOrZero(doc, "agentId");
            if (agentId > 0 && !nameMap.containsKey(agentId)) {
                String name = doc.getString("agentName");
                if (name != null) {
                    nameMap.put(agentId, name);
                }
            }
        }
        return nameMap;
    }

    /**
     * 종합 순위 점수 계산 (0~1 스케일)
     * 배치 PerformanceTasklet과 동일 로직
     */
    private double calculateCompositeScore(
            int consultCount, double avgDuration, double quality, double satisfaction,
            double minConsult, double maxConsult, double minDuration, double maxDuration) {

        double consultNorm = (maxConsult == minConsult)
                ? 1.0 : (consultCount - minConsult) / (maxConsult - minConsult);

        double durationNorm = (maxDuration == minDuration)
                ? 1.0 : 1.0 - (avgDuration - minDuration) / (maxDuration - minDuration);

        double qualityNorm = quality / 5.0;
        double satisfactionNorm = satisfaction / 5.0;

        return consultNorm * 0.25 + durationNorm * 0.15 + qualityNorm * 0.30 + satisfactionNorm * 0.30;
    }

    /** 순위 계산용 임시 구조체 */
    private static class AgentScoreHolder {
        final long agentId;
        final int consultCount;
        final double avgDuration;
        final double satisfaction;
        final double quality;
        double compositeScore;

        AgentScoreHolder(long agentId, int consultCount, double avgDuration,
                         double satisfaction, double quality) {
            this.agentId = agentId;
            this.consultCount = consultCount;
            this.avgDuration = avgDuration;
            this.satisfaction = satisfaction;
            this.quality = quality;
        }
    }

    // ==================== Helper (공통) ====================

    private int getIntOrZero(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private double getDoubleOrZero(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }

    private long getLongOrZero(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    private Document findSnapshot(LocalDateTime startAt) {
        Query query = new Query(Criteria.where("startAt").is(startAt));
        return mongoTemplate.findOne(query, Document.class, COLLECTION);
    }

    /**
     * KeywordRankTasklet이 저장한 키워드 전용 문서 조회 (date 필드 기준)
     */
    private Document findKeywordSnapshot(LocalDate date) {
        Query query = new Query(Criteria.where("date").is(date));
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