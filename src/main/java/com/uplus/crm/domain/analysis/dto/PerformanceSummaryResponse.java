package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 전체 상담 성과 요약 응답 DTO
 *
 * 주간/월간 공용. 스냅샷(weekly_report_snapshot / monthly_report_snapshot)에서 조회.
 * - 총 상담 수
 * - 상담사별 평균 처리 건수
 * - 평균 소요 시간(초)
 * - 평균 고객 만족도
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 상담 성과 요약")
public class PerformanceSummaryResponse {

    @Schema(description = "집계 시작일", example = "2025-01-13")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-19")
    private String endDate;

    @Schema(description = "총 상담 수", example = "5365")
    private Integer totalConsultCount;

    @Schema(description = "상담사별 평균 처리 건수", example = "178.8")
    private Double avgConsultCountPerAgent;

    @Schema(description = "평균 소요 시간 (초)", example = "315.0")
    private Double avgDurationMinutes;

    @Schema(description = "평균 고객 만족도 (0~5)", example = "4.5")
    private Double avgSatisfiedScore;
}
