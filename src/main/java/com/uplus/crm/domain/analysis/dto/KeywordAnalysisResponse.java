package com.uplus.crm.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.Document;

/**
 * 주간/월간 키워드 분석 응답 DTO
 *
 * weekly_report_snapshot / monthly_report_snapshot의 keywordSummary 섹션을 매핑합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeywordAnalysisResponse {

    private String startDate;
    private String endDate;
    private List<TopKeyword> topKeywords;
    private List<LongTermKeyword> longTermTopKeywords;
    private List<CustomerTypeKeyword> byCustomerType;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopKeyword {
        private String keyword;
        private long count;
        private int rank;
        private double changeRate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LongTermKeyword {
        private String keyword;
        private long count;
        private int rank;
        private int appearanceDays;
        private int totalDays;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerTypeKeyword {
        private String customerType;
        private List<CustomerKeywordCount> keywords;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerKeywordCount {
        private String keyword;
        private long count;
    }

    /**
     * MongoDB Document(스냅샷)에서 KeywordAnalysisResponse를 생성합니다.
     */
    public static KeywordAnalysisResponse from(Document snapshot) {
        if (snapshot == null) {
            return null;
        }

        Document kwSummary = snapshot.get("keywordSummary", Document.class);
        if (kwSummary == null) {
            return null;
        }

        // startAt / endAt 추출
        String startDate = toDateString(snapshot, "startAt");
        String endDate = toDateString(snapshot, "endAt");

        // topKeywords 파싱
        List<Document> topDocs = kwSummary.getList("topKeywords", Document.class);
        List<TopKeyword> topKeywords = topDocs == null ? List.of() :
                topDocs.stream().map(k -> TopKeyword.builder()
                        .keyword(k.getString("keyword"))
                        .count(getNumber(k, "count"))
                        .rank(k.getInteger("rank", 0))
                        .changeRate(getDouble(k, "changeRate"))
                        .build()).collect(Collectors.toList());

        // longTermTopKeywords 파싱
        List<Document> longTermDocs = kwSummary.getList("longTermTopKeywords", Document.class);
        List<LongTermKeyword> longTermKeywords = longTermDocs == null ? List.of() :
                longTermDocs.stream().map(k -> LongTermKeyword.builder()
                        .keyword(k.getString("keyword"))
                        .count(getNumber(k, "count"))
                        .rank(k.getInteger("rank", 0))
                        .appearanceDays(k.getInteger("appearanceDays", 0))
                        .totalDays(k.getInteger("totalDays", 0))
                        .build()).collect(Collectors.toList());

        // byCustomerType 파싱 (keyword+count Document 또는 String 리스트 호환)
        List<Document> ctDocs = kwSummary.getList("byCustomerType", Document.class);
        List<CustomerTypeKeyword> byCustomerType = ctDocs == null ? List.of() :
                ctDocs.stream().map(ct -> {
                    List<CustomerKeywordCount> keywords = parseCustomerKeywords(ct);
                    return CustomerTypeKeyword.builder()
                            .customerType(ct.getString("customerType"))
                            .keywords(keywords)
                            .build();
                }).collect(Collectors.toList());

        return KeywordAnalysisResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .topKeywords(topKeywords)
                .longTermTopKeywords(longTermKeywords)
                .byCustomerType(byCustomerType)
                .build();
    }

    // ==================== Helper ====================

    @SuppressWarnings("unchecked")
    private static List<CustomerKeywordCount> parseCustomerKeywords(Document ct) {
        Object raw = ct.get("keywords");
        if (raw == null) return List.of();

        if (raw instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Document) {
                // 신규 포맷: [{keyword, count}, ...]
                return ((List<Document>) raw).stream()
                        .map(kw -> CustomerKeywordCount.builder()
                                .keyword(kw.getString("keyword"))
                                .count(getNumber(kw, "count"))
                                .build())
                        .collect(Collectors.toList());
            } else if (first instanceof String) {
                // 레거시 포맷: ["키워드1", "키워드2", ...]
                return ((List<String>) raw).stream()
                        .map(keyword -> CustomerKeywordCount.builder()
                                .keyword(keyword)
                                .count(0)
                                .build())
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    private static String toDateString(Document doc, String field) {
        Object val = doc.get(field);
        if (val instanceof LocalDateTime) {
            return ((LocalDateTime) val).toLocalDate().toString();
        }
        if (val instanceof Date) {
            return ((Date) val).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().toString();
        }
        return val != null ? val.toString() : null;
    }

    private static long getNumber(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }

    private static double getDouble(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }
}
