package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.SubscriptionAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 전체 상담 성과 리포트 Service (주간/월간 공용)
 *
 * 배치가 생성한 스냅샷(weekly_report_snapshot / monthly_report_snapshot)에서 조회.
 * 컬렉션명만 달리하여 주간/월간 리포트를 동일 로직으로 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceReportService {

    private final MongoTemplate mongoTemplate;

    private static final String WEEKLY_COLLECTION = "weekly_report_snapshot";
    private static final String MONTHLY_COLLECTION = "monthly_report_snapshot";

    // ==================== 주간 ====================

    public Optional<PerformanceSummaryResponse> getWeeklyPerformanceSummary(LocalDate date) {
        return getPerformanceSummary(WEEKLY_COLLECTION, date);
    }

    public Optional<AgentRankingResponse> getWeeklyAgentRanking(LocalDate date) {
        return getAgentRanking(WEEKLY_COLLECTION, date);
    }

    public Optional<KeywordAnalysisResponse> getWeeklyKeywordAnalysis(LocalDate date) {
        return getKeywordAnalysis(WEEKLY_COLLECTION, date);
    }

    // ==================== 월간 ====================

    public Optional<PerformanceSummaryResponse> getMonthlyPerformanceSummary(LocalDate date) {
        return getPerformanceSummary(MONTHLY_COLLECTION, date);
    }

    public Optional<AgentRankingResponse> getMonthlyAgentRanking(LocalDate date) {
        return getAgentRanking(MONTHLY_COLLECTION, date);
    }

    public Optional<KeywordAnalysisResponse> getMonthlyKeywordAnalysis(LocalDate date) {
        return getKeywordAnalysis(MONTHLY_COLLECTION, date);
    }

    // ==================== 구독상품 선호도 ====================

    public Optional<SubscriptionAnalysisResponse> getWeeklySubscription(LocalDate date) {
        return getSubscription(WEEKLY_COLLECTION, date);
    }

    public Optional<SubscriptionAnalysisResponse> getMonthlySubscription(LocalDate date) {
        return getSubscription(MONTHLY_COLLECTION, date);
    }

    private Optional<SubscriptionAnalysisResponse> getSubscription(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date, "subscriptionAnalysis");

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음 (구독)", collection, date);
            return Optional.empty();
        }

        SubscriptionAnalysisResponse response = SubscriptionAnalysisResponse.from(snapshot);
        if (response == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷에 subscriptionAnalysis 없음", collection, date);
            return Optional.empty();
        }
        return Optional.of(response);
    }

    // ==================== 공통 로직 ====================

    /**
     * 전체 성과 요약 조회
     *
     * date가 포함되는 스냅샷(startAt <= date < endAt)을 찾아 요약 데이터를 반환합니다.
     */
    private Optional<PerformanceSummaryResponse> getPerformanceSummary(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date, "totalConsultCount");

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음", collection, date);
            return Optional.empty();
        }

        Document perfSummary = snapshot.get("performanceSummary", Document.class);

        return Optional.of(PerformanceSummaryResponse.builder()
                .startDate(toDateString(snapshot, "startAt"))
                .endDate(toDateString(snapshot, "endAt"))
                .totalConsultCount(getIntOrZero(snapshot, "totalConsultCount"))
                .avgConsultCountPerAgent(perfSummary != null
                        ? getDoubleOrZero(perfSummary, "avgConsultPerAgent") : 0.0)
                .avgDurationMinutes(getDoubleOrZero(snapshot, "avgDurationMinutes"))
                .avgSatisfiedScore(perfSummary != null
                        ? getDoubleOrZero(perfSummary, "avgSatisfiedScore") : 0.0)
                .build());
    }

    /**
     * 상담사 성과 순위 조회 (TOP 10)
     *
     * 스냅샷의 agentPerformance 배열에서 consultCount 내림차순 상위 10명을 반환합니다.
     */
    private Optional<AgentRankingResponse> getAgentRanking(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date, "performanceSummary");

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음", collection, date);
            return Optional.empty();
        }

        Document perfSummary = snapshot.get("performanceSummary", Document.class);
        if (perfSummary == null) return Optional.empty();

        List<Document> agentDocs = perfSummary.getList("agentRanking", Document.class);
        List<AgentRankingResponse.AgentPerformance> agents = new ArrayList<>();

        if (agentDocs != null) {
            int limit = Math.min(agentDocs.size(), 10);
            for (int i = 0; i < limit; i++) {
                Document doc = agentDocs.get(i);
                agents.add(AgentRankingResponse.AgentPerformance.builder()
                        .rank(getIntOrZero(doc, "rank"))
                        .agentId(getLongOrZero(doc, "agentId"))
                        .agentName(doc.getString("agentName"))
                        .consultCount(getIntOrZero(doc, "consultCount"))
                        .avgDurationMinutes(getDoubleOrZero(doc, "avgDurationMinutes"))
                        .avgSatisfiedScore(getDoubleOrZero(doc, "avgSatisfiedScore"))
                        .qualityScore(getDoubleOrZero(doc, "qualityScore"))
                        .build());
            }
        }

        return Optional.of(AgentRankingResponse.builder()
                .startDate(toDateString(snapshot, "startAt"))
                .endDate(toDateString(snapshot, "endAt"))
                .agents(agents)
                .build());
    }

    /**
     * 키워드 분석 조회
     *
     * 스냅샷의 keywordSummary에서 topKeywords, longTermTopKeywords, byCustomerType를 반환합니다.
     */
    private Optional<KeywordAnalysisResponse> getKeywordAnalysis(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date, "keywordSummary");

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음 (키워드)", collection, date);
            return Optional.empty();
        }

        KeywordAnalysisResponse response = KeywordAnalysisResponse.from(snapshot);
        if (response == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷에 keywordSummary 없음", collection, date);
            return Optional.empty();
        }
        return Optional.of(response);
    }

    // ==================== Helper ====================

    /**
     * date가 포함되는 스냅샷을 찾습니다.
     * startAt <= date <= endAt 범위에 해당하는 스냅샷만 반환합니다.
     */
    private Document findSnapshotContaining(String collection, LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
        ).limit(1);
        return mongoTemplate.findOne(query, Document.class, collection);
    }

    /**
     * date가 포함되고, 특정 필드가 존재하는 스냅샷을 찾습니다.
     * 같은 컬렉션에 여러 스냅샷이 존재할 때 필요한 데이터가 있는 문서를 정확히 조회합니다.
     */
    private Document findSnapshotContaining(String collection, LocalDate date, String requiredField) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
                        .and(requiredField).exists(true)
        ).limit(1);
        return mongoTemplate.findOne(query, Document.class, collection);
    }

    private String toDateString(Document doc, String field) {
        Object val = doc.get(field);
        if (val instanceof LocalDateTime) {
            return ((LocalDateTime) val).toLocalDate().toString();
        }
        if (val instanceof java.util.Date) {
            return ((java.util.Date) val).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate().toString();
        }
        return val != null ? val.toString() : null;
    }

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

    private Double getDoubleOrNull(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : null;
    }
}
