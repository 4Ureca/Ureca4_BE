package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.QualityAnalysisResponse;
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
 * 응대 품질 분석 조회 Service
 *
 * daily/weekly/monthly_agent_report_snapshot에서
 * qualityAnalysis 필드만 추출하여 반환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityAnalysisService {

    private final MongoTemplate mongoTemplate;

    private static final String DAILY_COLLECTION = "daily_agent_report_snapshot";
    private static final String WEEKLY_COLLECTION = "weekly_agent_report_snapshot";
    private static final String MONTHLY_COLLECTION = "monthly_agent_report_snapshot";

    // ==================== 일별 ====================

    public Optional<QualityAnalysisResponse> getDailyByAgent(Long agentId, LocalDate date) {
        Query query = new Query(
                Criteria.where("agentId").is(agentId)
                        .and("startAt").is(date.atStartOfDay())
        );
        Document doc = mongoTemplate.findOne(query, Document.class, DAILY_COLLECTION);
        return toResponse(doc);
    }

    public List<QualityAnalysisResponse> getDailyAll(LocalDate date) {
        Query query = new Query(
                Criteria.where("startAt").is(date.atStartOfDay())
                        .and("qualityAnalysis").ne(null)
        );
        return toResponseList(mongoTemplate.find(query, Document.class, DAILY_COLLECTION));
    }

    // ==================== 주별 ====================

    public Optional<QualityAnalysisResponse> getWeeklyByAgent(Long agentId, LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("agentId").is(agentId)
                        .and("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
        );
        Document doc = mongoTemplate.findOne(query, Document.class, WEEKLY_COLLECTION);
        return toResponse(doc);
    }

    public List<QualityAnalysisResponse> getWeeklyAll(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
                        .and("qualityAnalysis").ne(null)
        );
        return toResponseList(mongoTemplate.find(query, Document.class, WEEKLY_COLLECTION));
    }

    // ==================== 월별 ====================

    public Optional<QualityAnalysisResponse> getMonthlyByAgent(Long agentId, LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("agentId").is(agentId)
                        .and("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
        );
        Document doc = mongoTemplate.findOne(query, Document.class, MONTHLY_COLLECTION);
        return toResponse(doc);
    }

    public List<QualityAnalysisResponse> getMonthlyAll(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        Query query = new Query(
                Criteria.where("startAt").lte(dateTime)
                        .and("endAt").gte(dateTime)
                        .and("qualityAnalysis").ne(null)
        );
        return toResponseList(mongoTemplate.find(query, Document.class, MONTHLY_COLLECTION));
    }

    // ==================== 변환 ====================

    private Optional<QualityAnalysisResponse> toResponse(Document doc) {
        if (doc == null) return Optional.empty();

        Document qa = doc.get("qualityAnalysis", Document.class);
        if (qa == null) return Optional.empty();

        return Optional.of(buildResponse(doc, qa));
    }

    private List<QualityAnalysisResponse> toResponseList(List<Document> docs) {
        List<QualityAnalysisResponse> result = new ArrayList<>();
        for (Document doc : docs) {
            Document qa = doc.get("qualityAnalysis", Document.class);
            if (qa != null) {
                result.add(buildResponse(doc, qa));
            }
        }
        return result;
    }

    private QualityAnalysisResponse buildResponse(Document doc, Document qa) {
        return QualityAnalysisResponse.builder()
                .agentId(getLong(doc, "agentId"))
                .startDate(toDateString(doc, "startAt"))
                .endDate(toDateString(doc, "endAt"))
                .consultCount(getInt(doc, "consultCount"))
                .analyzedCount(getInt(qa, "analyzedCount"))
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
