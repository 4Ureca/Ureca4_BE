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

    public static TimeSlotTrendResponse from(LocalDate date, Document snapshot, String slot) {
        List<Document> trends = snapshot.getList("timeSlotTrend", Document.class);

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

            return SlotResult.builder()
                    .slot(t.getString("slot"))
                    .consultCount(t.getInteger("consultCount", 0))
                    .avgDuration(t.get("avgDuration") instanceof Number
                            ? ((Number) t.get("avgDuration")).doubleValue() : 0)
                    .categoryBreakdown(cats)
                    .build();
        }).collect(Collectors.toList());

        return TimeSlotTrendResponse.builder()
                .date(date.toString())
                .timeSlotTrend(slotResults)
                .build();
    }
}
