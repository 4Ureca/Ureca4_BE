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
 * 월별 해지방어 패턴 분석 응답 DTO
 */
@Schema(description = "해지방어 패턴 분석 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChurnDefenseResponse {

    @Schema(description = "집계 시작일", example = "2025-01-01")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-31")
    private String endDate;

    @Schema(description = "해지방어 시도 건수", example = "85")
    private Integer totalAttempts;

    @Schema(description = "방어 성공 건수", example = "52")
    private Integer successCount;

    @Schema(description = "방어 성공률 (%)", example = "61.2")
    private Double successRate;

    @Schema(description = "해지 의향 상담 평균 소요 시간(초)", example = "520")
    private Integer avgDurationSec;

    @Schema(description = "불만 사유별 방어율")
    private List<ComplaintReason> complaintReasons;

    @Schema(description = "고객 유형별 해지 분석 (연령+성별)")
    private List<CustomerTypeDefense> byCustomerType;

    @Schema(description = "상담사 액션별 현황")
    private List<ActionDefense> byAction;

    // ==================== 내부 DTO ====================

    @Schema(description = "불만 사유별 방어율")
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ComplaintReason {
        @Schema(description = "사유", example = "요금 불만")
        private String reason;
        @Schema(description = "방어 시도 건수", example = "85")
        private int attempts;
        @Schema(description = "성공 건수", example = "52")
        private int successCount;
        @Schema(description = "성공률 (%)", example = "61.2")
        private double successRate;
        @Schema(description = "평균 소요 시간(초)", example = "522")
        private int avgDurationSec;
    }

    @Schema(description = "고객 유형별 해지 분석")
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerTypeDefense {
        @Schema(description = "고객 유형 (연령+성별)", example = "20대 남성")
        private String type;
        @Schema(description = "주요 불만 사유", example = "경쟁사 이동")
        private String mainComplaintReason;
        @Schema(description = "건수", example = "18")
        private int attempts;
        @Schema(description = "방어 성공률 (%)", example = "38.9")
        private double successRate;
    }

    @Schema(description = "상담사 액션별 현황")
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActionDefense {
        @Schema(description = "방어 액션", example = "요금할인")
        private String action;
        @Schema(description = "시도 건수", example = "42")
        private int attempts;
        @Schema(description = "성공률 (%)", example = "68.2")
        private double successRate;
    }

    // ==================== Factory ====================

    public static ChurnDefenseResponse from(Document snapshot) {
        if (snapshot == null) return null;

        Document defense = snapshot.get("churnDefenseAnalysis", Document.class);
        if (defense == null) return null;

        String startDate = toDateString(snapshot, "startAt");
        String endDate = toDateString(snapshot, "endAt");

        // 불만 사유
        List<Document> reasonDocs = defense.getList("complaintReasons", Document.class);
        List<ComplaintReason> complaintReasons = reasonDocs == null ? List.of() :
                reasonDocs.stream().map(d -> ComplaintReason.builder()
                        .reason(d.getString("reason"))
                        .attempts(getInt(d, "attempts"))
                        .successCount(getInt(d, "successCount"))
                        .successRate(getDouble(d, "successRate"))
                        .avgDurationSec(getInt(d, "avgDurationSec"))
                        .build()).collect(Collectors.toList());

        // 고객 유형별
        List<Document> ctDocs = defense.getList("byCustomerType", Document.class);
        List<CustomerTypeDefense> byCustomerType = ctDocs == null ? List.of() :
                ctDocs.stream().map(d -> CustomerTypeDefense.builder()
                        .type(d.getString("type"))
                        .mainComplaintReason(d.getString("mainComplaintReason"))
                        .attempts(getInt(d, "attempts"))
                        .successRate(getDouble(d, "successRate"))
                        .build()).collect(Collectors.toList());

        // 방어 액션별
        List<Document> actionDocs = defense.getList("byAction", Document.class);
        List<ActionDefense> byAction = actionDocs == null ? List.of() :
                actionDocs.stream().map(d -> ActionDefense.builder()
                        .action(d.getString("action"))
                        .attempts(getInt(d, "attempts"))
                        .successRate(getDouble(d, "successRate"))
                        .build()).collect(Collectors.toList());

        return ChurnDefenseResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalAttempts(getInt(defense, "totalAttempts"))
                .successCount(getInt(defense, "successCount"))
                .successRate(getDouble(defense, "successRate"))
                .avgDurationSec(getInt(defense, "avgDurationSec"))
                .complaintReasons(complaintReasons)
                .byCustomerType(byCustomerType)
                .byAction(byAction)
                .build();
    }

    // ==================== Helper ====================

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

    private static int getInt(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private static double getDouble(Document doc, String field) {
        Object val = doc.get(field);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }
}
