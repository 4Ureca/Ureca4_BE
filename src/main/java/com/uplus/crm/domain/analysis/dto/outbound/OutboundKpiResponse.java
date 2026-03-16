package com.uplus.crm.domain.analysis.dto.outbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "아웃바운드 KPI 요약")
public class OutboundKpiResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "총 발신 건수", example = "50")
    private Integer totalCount;

    @Schema(description = "전환 성공 건수", example = "17")
    private Integer convertedCount;

    @Schema(description = "거절 건수", example = "33")
    private Integer rejectedCount;

    @Schema(description = "전환율 (%)", example = "34.0")
    private Double conversionRate;

    @Schema(description = "평균 통화 시간 (초)", example = "230.0")
    private Double avgDurationSec;

    @Schema(description = "예상 매출 합계", example = "850000")
    private Long estimatedRevenue;
}
