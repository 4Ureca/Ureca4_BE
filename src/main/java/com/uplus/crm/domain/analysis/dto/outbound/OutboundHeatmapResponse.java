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
@Schema(description = "아웃바운드 시간대×요일 전환율 히트맵")
public class OutboundHeatmapResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "지표명", example = "conversionRate")
    private String metric;

    @Schema(description = "시간대별 전환율 데이터")
    private List<HeatmapRow> rows;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapRow {
        @Schema(description = "시간 (0~23)", example = "10")
        private Integer hour;
        @Schema(description = "요일별 전환율 [월,화,수,목,금,토,일]")
        private List<Double> days;
    }
}
