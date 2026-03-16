package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.analysis.dto.outbound.*;
import com.uplus.crm.domain.common.entity.AnalysisCode;
import com.uplus.crm.domain.common.repository.AnalysisCodeRepository;
import com.uplus.crm.domain.common.repository.ConsultationCategoryPolicyRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 아웃바운드 리포트 서비스
 *
 * 기존 스냅샷 컬렉션(daily/weekly/monthly_report_snapshot)의
 * outboundAnalysis 필드에서 데이터를 읽어 각 API 응답을 생성.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundReportService {

    private final MongoTemplate mongoTemplate;
    private final ConsultationCategoryPolicyRepository categoryPolicyRepository;
    private final AnalysisCodeRepository analysisCodeRepository;

    private static final Map<String, String> COLLECTION_MAP = Map.of(
            "daily", "daily_report_snapshot",
            "weekly", "weekly_report_snapshot",
            "monthly", "monthly_report_snapshot"
    );

    /** outbound_category 코드 → AnalysisCode 매핑 (DB에서 로딩) */
    private Map<String, AnalysisCode> getRejectReasonCodeMap() {
        return analysisCodeRepository.findByClassification("outbound_category")
                .stream()
                .collect(Collectors.toMap(
                        AnalysisCode::getCodeName,
                        ac -> ac,
                        (a, b) -> a));
    }

    // ==================== API 1: KPI ====================

    public Optional<OutboundKpiResponse> getKpi(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        Document kpi = snapshot.get("kpi", Document.class);
        if (kpi == null) return Optional.empty();

        return Optional.of(OutboundKpiResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .totalCount(kpi.getInteger("totalCount", 0))
                .convertedCount(kpi.getInteger("convertedCount", 0))
                .rejectedCount(kpi.getInteger("rejectedCount", 0))
                .conversionRate(getDoubleOrZero(kpi, "conversionRate"))
                .avgDurationSec(getDoubleOrZero(kpi, "avgDurationSec"))
                .estimatedRevenue(getLongOrZero(kpi, "estimatedRevenue"))
                .build());
    }

    // ==================== API 2: 캠페인 성과 ====================

    public Optional<OutboundCampaignResponse> getCampaigns(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        List<Document> campaigns = snapshot.getList("campaigns", Document.class);
        if (campaigns == null || campaigns.isEmpty()) return Optional.empty();

        // MySQL에서 캠페인 활성 상태 조회
        Map<String, Boolean> activeMap = categoryPolicyRepository.findByIsActiveTrueOrderBySortOrder()
                .stream()
                .collect(Collectors.toMap(
                        ConsultationCategoryPolicy::getCategoryCode,
                        ConsultationCategoryPolicy::getIsActive,
                        (a, b) -> a));

        int totalCount = 0;
        int totalConverted = 0;
        double totalDuration = 0;
        double totalSatisfied = 0;
        int satisfiedCount = 0;
        long totalRevenue = 0;

        List<OutboundCampaignResponse.CampaignDetail> details = new ArrayList<>();
        for (Document c : campaigns) {
            String code = c.getString("categoryCode");
            int count = c.getInteger("totalCount", 0);
            int converted = c.getInteger("convertedCount", 0);

            totalCount += count;
            totalConverted += converted;
            totalDuration += getDoubleOrZero(c, "avgDurationSec") * count;
            double sat = getDoubleOrZero(c, "avgSatisfiedScore");
            if (sat > 0) {
                totalSatisfied += sat * count;
                satisfiedCount += count;
            }
            totalRevenue += getLongOrZero(c, "estimatedRevenue");

            details.add(OutboundCampaignResponse.CampaignDetail.builder()
                    .categoryCode(code)
                    .categoryName(c.getString("categoryName"))
                    .isActive(activeMap.getOrDefault(code, false))
                    .totalCount(count)
                    .convertedCount(converted)
                    .conversionRate(getDoubleOrZero(c, "conversionRate"))
                    .avgDurationSec(getDoubleOrZero(c, "avgDurationSec"))
                    .avgSatisfiedScore(getDoubleOrZero(c, "avgSatisfiedScore"))
                    .estimatedRevenue(getLongOrZero(c, "estimatedRevenue"))
                    .build());
        }

        OutboundCampaignResponse.CampaignTotal total = OutboundCampaignResponse.CampaignTotal.builder()
                .totalCount(totalCount)
                .conversionRate(totalCount > 0 ? Math.round((double) totalConverted / totalCount * 1000.0) / 10.0 : 0)
                .avgDurationSec(totalCount > 0 ? Math.round(totalDuration / totalCount * 10.0) / 10.0 : 0)
                .avgSatisfiedScore(satisfiedCount > 0 ? Math.round(totalSatisfied / satisfiedCount * 10.0) / 10.0 : 0)
                .estimatedRevenue(totalRevenue)
                .build();

        return Optional.of(OutboundCampaignResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .campaigns(details)
                .total(total)
                .build());
    }

    // ==================== API 3: 발신 결과 + 거절 사유 ====================

    public Optional<OutboundCallResultResponse> getCallResults(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        Document callResults = snapshot.get("callResults", Document.class);
        if (callResults == null) return Optional.empty();

        List<Document> distribution = callResults.getList("distribution", Document.class);
        int converted = 0, rejected = 0;
        if (distribution != null) {
            for (Document d : distribution) {
                if ("CONVERTED".equals(d.getString("result"))) converted = d.getInteger("count", 0);
                if ("REJECTED".equals(d.getString("result"))) rejected = d.getInteger("count", 0);
            }
        }
        int total = converted + rejected;

        OutboundCallResultResponse.Distribution dist = OutboundCallResultResponse.Distribution.builder()
                .converted(OutboundCallResultResponse.ResultCount.builder()
                        .count(converted)
                        .rate(total > 0 ? Math.round((double) converted / total * 1000.0) / 10.0 : 0)
                        .build())
                .rejected(OutboundCallResultResponse.ResultCount.builder()
                        .count(rejected)
                        .rate(total > 0 ? Math.round((double) rejected / total * 1000.0) / 10.0 : 0)
                        .build())
                .build();

        List<Document> reasons = callResults.getList("rejectReasons", Document.class);
        int totalReject = rejected;
        List<OutboundCallResultResponse.RejectReason> rejectReasons = new ArrayList<>();
        if (reasons != null) {
            Map<String, AnalysisCode> codeMap = getRejectReasonCodeMap();
            int rank = 1;
            for (Document r : reasons) {
                int count = r.getInteger("count", 0);
                String code = r.getString("code");
                AnalysisCode ac = codeMap.get(code);
                rejectReasons.add(OutboundCallResultResponse.RejectReason.builder()
                        .code(code)
                        .name(ac != null && ac.getDisplayName() != null ? ac.getDisplayName() : code)
                        .description(ac != null ? ac.getDescription() : null)
                        .count(count)
                        .rate(totalReject > 0 ? Math.round((double) count / totalReject * 1000.0) / 10.0 : 0)
                        .rank(rank++)
                        .build());
            }
        }

        return Optional.of(OutboundCallResultResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .distribution(dist)
                .rejectReasons(rejectReasons)
                .build());
    }

    // ==================== API 4: 히트맵 ====================

    public Optional<OutboundHeatmapResponse> getHeatmap(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        Document heatmap = snapshot.get("heatmap", Document.class);
        if (heatmap == null) return Optional.empty();

        List<Document> rows = heatmap.getList("conversionRate", Document.class);
        if (rows == null) return Optional.empty();

        List<OutboundHeatmapResponse.HeatmapRow> heatmapRows = rows.stream()
                .map(r -> OutboundHeatmapResponse.HeatmapRow.builder()
                        .hour(r.getInteger("hour", 0))
                        .days(toDoubleList(r.getList("days", Number.class)))
                        .build())
                .collect(Collectors.toList());

        return Optional.of(OutboundHeatmapResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .metric("conversionRate")
                .rows(heatmapRows)
                .build());
    }

    // ==================== API 5: 상담사별 실적 ====================

    public Optional<OutboundAgentResponse> getAgents(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        List<Document> agents = snapshot.getList("agents", Document.class);
        if (agents == null || agents.isEmpty()) return Optional.empty();

        List<OutboundAgentResponse.AgentDetail> details = agents.stream()
                .map(a -> OutboundAgentResponse.AgentDetail.builder()
                        .rank(a.getInteger("rank", 0))
                        .agentId(getLongOrZero(a, "agentId"))
                        .agentName(a.getString("agentName"))
                        .totalCount(a.getInteger("totalCount", 0))
                        .convertedCount(a.getInteger("convertedCount", 0))
                        .conversionRate(getDoubleOrZero(a, "conversionRate"))
                        .avgDurationSec(getDoubleOrZero(a, "avgDurationSec"))
                        .build())
                .collect(Collectors.toList());

        return Optional.of(OutboundAgentResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .agents(details)
                .build());
    }

    // ==================== API 6: 최적 연락 시간 ====================

    public Optional<OutboundOptimalTimeResponse> getOptimalTime(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        List<Document> optimalTime = snapshot.getList("optimalTime", Document.class);
        if (optimalTime == null || optimalTime.isEmpty()) return Optional.empty();

        List<OutboundOptimalTimeResponse.Recommendation> recommendations = optimalTime.stream()
                .map(o -> OutboundOptimalTimeResponse.Recommendation.builder()
                        .categoryCode(o.getString("categoryCode"))
                        .categoryName(o.getString("categoryName"))
                        .bestHourRange(o.getString("bestHourRange"))
                        .bestConversionRate(getDoubleOrZero(o, "bestConversionRate"))
                        .bestDays(o.getList("bestDays", String.class))
                        .build())
                .collect(Collectors.toList());

        return Optional.of(OutboundOptimalTimeResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .recommendations(recommendations)
                .build());
    }

    // ==================== API 7: 카테고리별 전환율 ====================

    public Optional<OutboundConversionResponse> getConversionByCategory(String period, LocalDate date) {
        Document snapshot = findSnapshot(period, date);
        if (snapshot == null) return Optional.empty();

        List<Document> conversionByCategory = snapshot.getList("conversionByCategory", Document.class);
        if (conversionByCategory == null || conversionByCategory.isEmpty()) return Optional.empty();

        List<OutboundConversionResponse.CategoryConversion> categories = conversionByCategory.stream()
                .map(c -> OutboundConversionResponse.CategoryConversion.builder()
                        .categoryCode(c.getString("categoryCode"))
                        .categoryName(c.getString("categoryName"))
                        .convertedCount(c.getInteger("convertedCount", 0))
                        .totalCount(c.getInteger("totalCount", 0))
                        .conversionRate(getDoubleOrZero(c, "conversionRate"))
                        .build())
                .collect(Collectors.toList());

        return Optional.of(OutboundConversionResponse.builder()
                .startAt(toLocalDateTime(snapshot.get("_startAt")))
                .endAt(toLocalDateTime(snapshot.get("_endAt")))
                .categories(categories)
                .build());
    }

    // ==================== snapshot 조회 ====================

    /**
     * 스냅샷 조회 — outboundAnalysis에 startAt/endAt을 포함시켜 반환
     */
    private Document findSnapshot(String period, LocalDate date) {
        String collection = COLLECTION_MAP.get(period.toLowerCase());
        if (collection == null) throw new BusinessException(ErrorCode.INVALID_PERIOD);

        Document snapshot;
        if (date == null) {
            Query query = new Query()
                    .with(org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC, "startAt"))
                    .limit(1);
            snapshot = mongoTemplate.findOne(query, Document.class, collection);
        } else {
            LocalDateTime[] range = resolveDateRange(period, date);
            LocalDateTime startAt = range[0];
            Query query = new Query(Criteria.where("startAt").is(startAt));
            snapshot = mongoTemplate.findOne(query, Document.class, collection);
        }
        if (snapshot == null) return null;

        Document outbound = snapshot.get("outboundAnalysis", Document.class);
        if (outbound == null) return null;

        // 상위 스냅샷의 startAt/endAt을 outbound에 첨부
        outbound.put("_startAt", snapshot.get("startAt"));
        outbound.put("_endAt", snapshot.get("endAt"));
        return outbound;
    }

    private LocalDateTime[] resolveDateRange(String period, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return switch (period.toLowerCase()) {
            case "daily" -> {
                LocalDateTime start = targetDate.atStartOfDay();
                yield new LocalDateTime[]{start, start.plusDays(1)};
            }
            case "weekly" -> {
                LocalDate monday = targetDate.with(DayOfWeek.MONDAY);
                yield new LocalDateTime[]{monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay()};
            }
            case "monthly" -> {
                LocalDate first = targetDate.withDayOfMonth(1);
                yield new LocalDateTime[]{first.atStartOfDay(), first.plusMonths(1).atStartOfDay()};
            }
            default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
        };
    }

    // ==================== 유틸 ====================

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Date) return ((Date) value).toInstant().atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    private double getDoubleOrZero(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }

    private long getLongOrZero(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    private List<Double> toDoubleList(List<Number> numbers) {
        if (numbers == null) return Collections.emptyList();
        return numbers.stream()
                .map(n -> n != null ? n.doubleValue() : 0.0)
                .collect(Collectors.toList());
    }
}
