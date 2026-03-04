package com.uplus.crm.domain.analysis.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class CategorySummaryResponse {

    private String date;
    private String slot;
    private int totalConsultCount;
    private List<CategoryItem> categories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {
        private String code;
        private String name;
        private int count;
        private double rate;
    }

    public static CategorySummaryResponse from(LocalDate date, Document snapshot, String slot) {
        List<Document> trends = snapshot.getList("timeSlotTrend", Document.class);

        List<Document> targetTrends = (slot != null)
                ? trends.stream()
                    .filter(t -> slot.equals(t.getString("slot")))
                    .collect(Collectors.toList())
                : trends;

        Map<String, Integer> codeToCount = new LinkedHashMap<>();
        Map<String, String> codeToName = new LinkedHashMap<>();
        int totalCount = 0;

        for (Document trend : targetTrends) {
            List<Document> catDocs = trend.getList("categoryBreakdown", Document.class);
            if (catDocs == null) continue;
            for (Document cat : catDocs) {
                String code = cat.getString("code");
                String name = cat.getString("name");
                int count = cat.getInteger("count", 0);
                codeToCount.merge(code, count, Integer::sum);
                codeToName.put(code, name);
                totalCount += count;
            }
        }

        final int finalTotal = totalCount;
        List<CategoryItem> categories = codeToCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> {
                    double rate = finalTotal > 0
                            ? Math.round(e.getValue() * 100.0 / finalTotal * 10.0) / 10.0
                            : 0;
                    return CategoryItem.builder()
                            .code(e.getKey())
                            .name(codeToName.get(e.getKey()))
                            .count(e.getValue())
                            .rate(rate)
                            .build();
                }).collect(Collectors.toList());

        return CategorySummaryResponse.builder()
                .date(date.toString())
                .slot(slot)
                .totalConsultCount(finalTotal)
                .categories(categories)
                .build();
    }
}
