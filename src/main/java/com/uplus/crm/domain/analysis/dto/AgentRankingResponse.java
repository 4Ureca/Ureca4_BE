package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 상담사 성과 순위 응답 DTO
 *
 * 주간/월간 공용. 스냅샷에서 상담사별 성과를 TOP 10 순위로 제공.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상담사 성과 순위 (TOP 10)")
public class AgentRankingResponse {

    @Schema(description = "집계 시작일", example = "2025-01-13")
    private String startDate;

    @Schema(description = "집계 종료일", example = "2025-01-19")
    private String endDate;

    @Schema(description = "상담사 성과 순위 목록 (최대 10명)")
    private List<AgentPerformance> agents;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 상담사 성과")
    public static class AgentPerformance {

        @Schema(description = "순위", example = "1")
        private int rank;

        @Schema(description = "상담사 ID", example = "5")
        private Long agentId;

        @Schema(description = "상담사 이름", example = "김민수")
        private String agentName;

        @Schema(description = "처리 건수", example = "48")
        private Integer consultCount;

        @Schema(description = "평균 소요 시간 (초)", example = "272.0")
        private Double avgDurationMinutes;

        @Schema(description = "평균 고객 만족도 (0~5)", example = "4.8")
        private Double avgSatisfiedScore;

        @Schema(description = "응대 품질 점수 (추후 구현)", example = "null")
        private Double qualityScore;
    }
}
