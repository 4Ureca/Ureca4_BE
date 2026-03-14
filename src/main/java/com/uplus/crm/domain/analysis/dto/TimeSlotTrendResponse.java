package com.uplus.crm.domain.analysis.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.Document;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotTrendResponse {

    private String date;
    private List<SlotResult> timeSlotTrend;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotResult {
        private String slot;
        private int consultCount;
        private double avgDuration;
        private List<CategoryBreakdown> categoryBreakdown;
        private KeywordAnalysis keywordAnalysis;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String code;
        private String name;
        private int count;
        private double rate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeywordAnalysis {
        private List<TopKeyword> topKeywords;
        private List<NewKeyword> newKeywords;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopKeyword {
        private String keyword;
        private int count;
        private int rank;
        private double changeRate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewKeyword {
        private String keyword;
        private int count;
    }

    public static TimeSlotTrendResponse from(LocalDate date, Document snapshot, String slot) {
        List<Document> trends = snapshot.getList("timeSlotTrend", Document.class);
        if (trends == null) trends = List.of();

        List<Document> filtered = (slot != null)
                ? trends.stream()
                    .filter(t -> slot.equals(t.getString("slot")))
                    .collect(Collectors.toList())
                : trends;

        List<SlotResult> slotResults = filtered.stream().map(t -> {
            List<Document> catDocs = t.getList("categoryBreakdown", Document.class);
            List<CategoryBreakdown> cats = catDocs == null ? List.of() :
                    catDocs.stream().map(c -> CategoryBreakdown.builder()
                            .code(c.getString("code"))
                            .name(c.getString("name"))
                            .count(c.getInteger("count", 0))
                            .rate(c.get("rate") instanceof Number
                                    ? ((Number) c.get("rate")).doubleValue() : 0)
                            .build()).collect(Collectors.toList());

            Document kwDoc = t.get("keywordAnalysis", Document.class);
            KeywordAnalysis keywordAnalysis = null;
            if (kwDoc != null) {
                List<Document> topDocs = kwDoc.getList("topKeywords", Document.class);
                List<TopKeyword> topKeywords = topDocs == null ? List.of() :
                        topDocs.stream().map(k -> TopKeyword.builder()
                                .keyword(k.getString("keyword"))
                                .count(k.get("count") instanceof Number
                                        ? ((Number) k.get("count")).intValue() : 0)
                                .rank(k.get("rank") instanceof Number
                                        ? ((Number) k.get("rank")).intValue() : 0)
                                .changeRate(k.get("changeRate") instanceof Number
                                        ? ((Number) k.get("changeRate")).doubleValue() : 0)
                                .build()).collect(Collectors.toList());

                List<Document> newDocs = kwDoc.getList("newKeywords", Document.class);
                List<NewKeyword> newKeywords = newDocs == null ? List.of() :
                        newDocs.stream().map(k -> NewKeyword.builder()
                                .keyword(k.getString("keyword"))
                                .count(k.get("count") instanceof Number
                                        ? ((Number) k.get("count")).intValue() : 0)
                                .build()).collect(Collectors.toList());

                keywordAnalysis = KeywordAnalysis.builder()
                        .topKeywords(topKeywords)
                        .newKeywords(newKeywords)
                        .build();
            }

            return SlotResult.builder()
                    .slot(t.getString("slot"))
                    .consultCount(t.getInteger("consultCount", 0))
                    .avgDuration(t.get("avgDuration") instanceof Number
                            ? ((Number) t.get("avgDuration")).doubleValue() : 0)
                    .categoryBreakdown(cats)
                    .keywordAnalysis(keywordAnalysis)
                    .build();
        }).collect(Collectors.toList());

        return TimeSlotTrendResponse.builder()
                .date(date.toString())
                .timeSlotTrend(slotResults)
                .build();
    }
}
