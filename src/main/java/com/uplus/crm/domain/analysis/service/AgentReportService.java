package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.AgentReportResponse;
import com.uplus.crm.domain.analysis.dto.AgentReportResponse.CategoryRanking;
import com.uplus.crm.domain.analysis.dto.AgentReportResponse.QualityAnalysis;
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
import java.util.List;
import java.util.Optional;

/**
 * 상담사 개인 리포트 조회 Service
 *
 * 배치가 생성한 daily/weekly/monthly_agent_report_snapshot에서 조회.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentReportService {

    private final MongoTemplate mongoTemplate;

    private static final String DAILY_COLLECTION = "daily_agent_report_snapshot";
    private static final String WEEKLY_COLLECTION = "weekly_agent_report_snapshot";
    private static final String MONTHLY_COLLECTION = "monthly_agent_report_snapshot";

    // ==================== 일별 ====================

    public Optional<AgentReportResponse> getDailyReport(Long agentId, LocalDate date) {
        LocalDateTime startAt = date.atStartOfDay();
        return findByAgentAndStartAt(DAILY_COLLECTION, agentId, startAt);
    }

    // ==================== 주별 ====================

    public Optional<AgentReportResponse> getWeeklyReport(Long agentId, LocalDate date) {
        return findByAgentContaining(WEEKLY_COLLECTION, agentId, date);
    }

    // ==================== 월별 ====================

    public Optional<AgentReportResponse> getMonthlyReport(Long agentId, LocalDate date) {
        return findByAgentContaining(MONTHLY_COLLECTION, agentId, date);
    }

    // ==================== 내부 로직 ====================

    /**
     * 일별: agentId + startAt 정확히 일치하는 스냅샷 조회
     */
    private Optional<AgentReportResponse> findByAgentAndStartAt(
            String collection, Long agentId, LocalDateTime startAt) {

        Query query = new Query(
                Criteria.where("agentId").is(agentId)
                        .and("startAt").is(startAt)
        );
        Document doc = mongoTemplate.findOne(query, Document.class, collection);

        if (doc == null) {
            log.info("[AgentReport] {} — agentId={}, startAt={} 스냅샷 없음", collection, agentId, startAt);
            return Optional.empty();
        }

        return Optional.of(toResponse(doc));
    }

    /**
     * 주별/월별: agentId + date가 startAt~endAt 범위에 포함되는 스냅샷 조회
     */
    private Optional<AgentReportResponse> findByAgentContaining(
            String collection, Long agentId, LocalDate date) {

        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("agentId").is(agentId)
                        .and("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
        );
        Document doc = mongoTemplate.findOne(query, Document.class, collection);

        if (doc == null) {
            log.info("[AgentReport] {} — agentId={}, date={} 스냅샷 없음", collection, agentId, date);
            return Optional.empty();
        }

        return Optional.of(toResponse(doc));
    }

    /**
     * MongoDB Document → AgentReportResponse 변환
     */
    private AgentReportResponse toResponse(Document doc) {
        return AgentReportResponse.builder()
                .agentId(getLong(doc, "agentId"))
                .startDate(toDateString(doc, "startAt"))
                .endDate(toDateString(doc, "endAt"))
                .consultCount(getInt(doc, "consultCount"))
                .avgDurationMinutes(getDouble(doc, "avgDurationMinutes"))
                .customerSatisfaction(getDouble(doc, "customerSatisfaction"))
                .categoryRanking(toCategoryRankings(doc))
                .qualityAnalysis(toQualityAnalysis(doc))
                .build();
    }

    private List<CategoryRanking> toCategoryRankings(Document doc) {
        List<Document> catDocs = doc.getList("categoryRanking", Document.class);
        if (catDocs == null) return List.of();

        List<CategoryRanking> result = new ArrayList<>();
        for (Document c : catDocs) {
            result.add(CategoryRanking.builder()
                    .rank(getInt(c, "rank"))
                    .code(c.getString("code"))
                    .large(c.getString("large"))
                    .medium(c.getString("medium"))
                    .count(getInt(c, "count"))
                    .build());
        }
        return result;
    }

    private QualityAnalysis toQualityAnalysis(Document doc) {
        Document qa = doc.get("qualityAnalysis", Document.class);
        if (qa == null) return null;

        return QualityAnalysis.builder()
                .empathyCount(getLong(qa, "empathyCount"))
                .avgEmpathyPerConsult(getDouble(qa, "avgEmpathyPerConsult"))
                .apologyRate(getDouble(qa, "apologyRate"))
                .closingRate(getDouble(qa, "closingRate"))
                .courtesyRate(getDouble(qa, "courtesyRate"))
                .promptnessRate(getDouble(qa, "promptnessRate"))
                .accuracyRate(getDouble(qa, "accuracyRate"))
                .waitingGuideRate(getDouble(qa, "waitingGuideRate"))
                .totalScore(getDouble(qa, "totalScore"))
                .build();
    }

    // ==================== Helper ====================

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

    private int getInt(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private long getLong(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    private double getDouble(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }
}
