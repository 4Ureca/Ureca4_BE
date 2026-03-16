package com.uplus.crm.domain.analysis.dto.outbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "아웃바운드 상담사별 실적")
public class OutboundAgentResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "상담사별 실적 목록 (전환 건수 내림차순)")
    private List<AgentDetail> agents;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentDetail {
        @Schema(description = "순위")
        private Integer rank;
        @Schema(description = "상담사 ID")
        private Long agentId;
        @Schema(description = "상담사 이름")
        private String agentName;
        @Schema(description = "총 발신 건수")
        private Integer totalCount;
        @Schema(description = "전환 성공 건수")
        private Integer convertedCount;
        @Schema(description = "전환율 (%)")
        private Double conversionRate;
        @Schema(description = "평균 통화 시간 (초)")
        private Double avgDurationSec;
    }
}
