package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
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

    // ==================== 월간 ====================

    public Optional<PerformanceSummaryResponse> getMonthlyPerformanceSummary(LocalDate date) {
        return getPerformanceSummary(MONTHLY_COLLECTION, date);
    }

    public Optional<AgentRankingResponse> getMonthlyAgentRanking(LocalDate date) {
        return getAgentRanking(MONTHLY_COLLECTION, date);
    }

    // ==================== 공통 로직 ====================

    /**
     * 전체 성과 요약 조회
     *
     * date가 포함되는 스냅샷(startAt <= date < endAt)을 찾아 요약 데이터를 반환합니다.
     */
    private Optional<PerformanceSummaryResponse> getPerformanceSummary(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date);

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음", collection, date);
            return Optional.empty();
        }

        return Optional.of(PerformanceSummaryResponse.builder()
                .startDate(toDateString(snapshot, "startAt"))
                .endDate(toDateString(snapshot, "endAt"))
                .totalConsultCount(getIntOrZero(snapshot, "totalConsultCount"))
                .avgConsultCountPerAgent(getDoubleOrZero(snapshot, "avgConsultCountPerAgent"))
                .avgDurationMinutes(getDoubleOrZero(snapshot, "avgDurationMinutes"))
                .avgSatisfiedScore(getDoubleOrZero(snapshot, "avgSatisfiedScore"))
                .build());
    }

    /**
     * 상담사 성과 순위 조회 (TOP 10)
     *
     * 스냅샷의 agentPerformance 배열에서 consultCount 내림차순 상위 10명을 반환합니다.
     */
    private Optional<AgentRankingResponse> getAgentRanking(String collection, LocalDate date) {
        Document snapshot = findSnapshotContaining(collection, date);

        if (snapshot == null) {
            log.info("[PerformanceReport] {} — {} 스냅샷 없음", collection, date);
            return Optional.empty();
        }

        List<Document> agentDocs = snapshot.getList("agentPerformance", Document.class);
        List<AgentRankingResponse.AgentPerformance> agents = new ArrayList<>();

        if (agentDocs != null) {
            int rank = 1;
            int limit = Math.min(agentDocs.size(), 10);
            for (int i = 0; i < limit; i++) {
                Document doc = agentDocs.get(i);
                agents.add(AgentRankingResponse.AgentPerformance.builder()
                        .rank(rank++)
                        .agentId(getLongOrZero(doc, "agentId"))
                        .agentName(doc.getString("agentName"))
                        .consultCount(getIntOrZero(doc, "consultCount"))
                        .avgDurationMinutes(getDoubleOrZero(doc, "avgDurationMinutes"))
                        .avgSatisfiedScore(getDoubleOrZero(doc, "avgSatisfiedScore"))
                        .qualityScore(getDoubleOrNull(doc, "qualityScore"))
                        .build());
            }
        }

        return Optional.of(AgentRankingResponse.builder()
                .startDate(toDateString(snapshot, "startAt"))
                .endDate(toDateString(snapshot, "endAt"))
                .agents(agents)
                .build());
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
