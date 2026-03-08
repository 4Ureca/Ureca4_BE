package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 상담사 개인 리포트 응답 DTO (일별/주별/월별 공용)
 *
 * daily/weekly/monthly_agent_report_snapshot에서 조회한 데이터를 반환한다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상담사 개인 리포트")
public class AgentReportResponse {

    @Schema(description = "상담사 ID", example = "1")
    private Long agentId;

    @Schema(description = "집계 시작일", example = "2025-01-13")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-19")
    private String endDate;

    @Schema(description = "상담 처리 건수", example = "15")
    private int consultCount;

    @Schema(description = "평균 소요 시간 (분)", example = "8.5")
    private double avgDurationMinutes;

    @Schema(description = "고객 만족도 (0~5)", example = "4.2")
    private double customerSatisfaction;

    @Schema(description = "처리 카테고리 순위")
    private List<CategoryRanking> categoryRanking;

    @Schema(description = "응대 품질 분석 (null이면 분석 데이터 없음)")
    private QualityAnalysis qualityAnalysis;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카테고리 순위")
    public static class CategoryRanking {

        @Schema(description = "순위", example = "1")
        private int rank;

        @Schema(description = "중분류 코드", example = "M_ETC_01")
        private String code;

        @Schema(description = "대분류", example = "기타")
        private String large;

        @Schema(description = "중분류", example = "일반 문의")
        private String medium;

        @Schema(description = "건수", example = "5")
        private int count;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "응대 품질 분석")
    public static class QualityAnalysis {

        @Schema(description = "공감 표현 총 횟수", example = "5")
        private long empathyCount;

        @Schema(description = "건당 평균 공감 횟수", example = "0.3")
        private double avgEmpathyPerConsult;

        @Schema(description = "사과 표현 포함 비율 (%)", example = "33.3")
        private double apologyRate;

        @Schema(description = "마무리 멘트 포함 비율 (%)", example = "60.0")
        private double closingRate;

        @Schema(description = "친절 표현 포함 비율 (%)", example = "20.0")
        private double courtesyRate;

        @Schema(description = "신속 응대 포함 비율 (%)", example = "13.3")
        private double promptnessRate;

        @Schema(description = "정확 응대 포함 비율 (%)", example = "6.7")
        private double accuracyRate;

        @Schema(description = "대기 안내 포함 비율 (%)", example = "40.0")
        private double waitingGuideRate;

        @Schema(description = "종합 점수 (0~5)", example = "2.1")
        private double totalScore;
    }
}
