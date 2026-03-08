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
public class KeywordRankingResponse {

    private String date;
    private String slot;
    private List<TopKeyword> topKeywords;
    private List<NewKeyword> newKeywords;

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
    public static class NewKeyword {
        private String keyword;
        private long count;
    }

    public static KeywordRankingResponse from(LocalDate date, Document snapshot, String slot) {
        List<Document> topDocs;
        List<Document> newDocs;

        if (slot != null) {
            List<Document> trends = snapshot.getList("timeSlotTrend", Document.class);
            Document targetTrend = trends == null ? null : trends.stream()
                    .filter(t -> slot.equals(t.getString("slot")))
                    .findFirst().orElse(null);

            Document kwAnalysis = targetTrend != null
                    ? targetTrend.get("keywordAnalysis", Document.class) : null;
            topDocs = kwAnalysis != null
                    ? kwAnalysis.getList("topKeywords", Document.class) : List.of();
            newDocs = kwAnalysis != null
                    ? kwAnalysis.getList("newKeywords", Document.class) : List.of();
        } else {
            Document kwSummary = snapshot.get("keywordSummary", Document.class);
            topDocs = kwSummary != null
                    ? kwSummary.getList("topKeywords", Document.class) : List.of();
            newDocs = List.of();
        }

        List<TopKeyword> topKeywords = topDocs == null ? List.of() :
                topDocs.stream().map(k -> TopKeyword.builder()
                        .keyword(k.getString("keyword"))
                        .count(k.get("count") instanceof Number
                                ? ((Number) k.get("count")).longValue() : 0)
                        .rank(k.getInteger("rank", 0))
                        .changeRate(k.get("changeRate") instanceof Number
                                ? ((Number) k.get("changeRate")).doubleValue() : 0)
                        .build()).collect(Collectors.toList());

        List<NewKeyword> newKeywords = newDocs == null ? List.of() :
                newDocs.stream().map(k -> NewKeyword.builder()
                        .keyword(k.getString("keyword"))
                        .count(k.get("count") instanceof Number
                                ? ((Number) k.get("count")).longValue() : 0)
                        .build()).collect(Collectors.toList());

        return KeywordRankingResponse.builder()
                .date(date.toString())
                .slot(slot)
                .topKeywords(topKeywords)
                .newKeywords(newKeywords)
                .build();
    }
}
