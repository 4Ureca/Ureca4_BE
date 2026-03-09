package com.uplus.crm.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 구독상품 선호도 분석 응답 DTO (주간/월간 공용)
 */
@Schema(description = "구독상품 선호도 분석 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionAnalysisResponse {

    @Schema(description = "집계 시작일", example = "2025-01-13")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-19")
    private String endDate;

    @Schema(description = "신규 가입 상위 상품 (TOP 6)")
    private List<ProductCount> newSubscriptions;

    @Schema(description = "해지 상위 상품 (TOP 6)")
    private List<ProductCount> canceledSubscriptions;

    @Schema(description = "연령대별 선호 상품")
    private List<AgeGroupPreference> byAgeGroup;

    // ==================== 내부 DTO ====================

    @Schema(description = "상품별 건수")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCount {

        @Schema(description = "상품 ID", example = "PROD_001")
        private String productId;

        @Schema(description = "상품명", example = "5G 프리미엄")
        private String productName;

        @Schema(description = "건수", example = "128")
        private long count;
    }

    @Schema(description = "연령대별 선호 상품")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeGroupPreference {

        @Schema(description = "연령대", example = "20대")
        private String ageGroup;

        @Schema(description = "선호 상품 (TOP 3)")
        private List<ProductCount> preferredProducts;
    }

    // ==================== Factory ====================

    public static SubscriptionAnalysisResponse from(Document snapshot) {
        if (snapshot == null) return null;

        Document sub = snapshot.get("subscriptionAnalysis", Document.class);
        if (sub == null) return null;

        String startDate = toDateString(snapshot, "startAt");
        String endDate = toDateString(snapshot, "endAt");

        List<ProductCount> newSubs = toProductCountList(sub.getList("newSubscriptions", Document.class));
        List<ProductCount> canceledSubs = toProductCountList(sub.getList("canceledSubscriptions", Document.class));

        List<Document> ageDocs = sub.getList("byAgeGroup", Document.class);
        List<AgeGroupPreference> byAgeGroup = ageDocs == null ? List.of() :
                ageDocs.stream().map(d -> AgeGroupPreference.builder()
                        .ageGroup(d.getString("ageGroup"))
                        .preferredProducts(toProductCountList(d.getList("preferredProducts", Document.class)))
                        .build()).collect(Collectors.toList());

        return SubscriptionAnalysisResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .newSubscriptions(newSubs)
                .canceledSubscriptions(canceledSubs)
                .byAgeGroup(byAgeGroup)
                .build();
    }

    // ==================== Helper ====================

    private static List<ProductCount> toProductCountList(List<Document> docs) {
        if (docs == null) return List.of();
        return docs.stream().map(d -> ProductCount.builder()
                .productId(d.getString("productId"))
                .productName(d.getString("productName"))
                .count(getlong(d, "count"))
                .build()).collect(Collectors.toList());
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

    private static long getlong(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).longValue() : 0L;
    }
}
