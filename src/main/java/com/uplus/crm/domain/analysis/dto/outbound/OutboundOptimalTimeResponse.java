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
@Schema(description = "아웃바운드 카테고리별 최적 연락 시간")
public class OutboundOptimalTimeResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "카테고리별 추천 시간")
    private List<Recommendation> recommendations;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        @Schema(description = "카테고리 코드", example = "M_OTB_01")
        private String categoryCode;
        @Schema(description = "카테고리명", example = "재약정 권유")
        private String categoryName;
        @Schema(description = "최적 시간대", example = "10:00 ~ 11:30")
        private String bestHourRange;
        @Schema(description = "해당 시간대 전환율 (%)")
        private Double bestConversionRate;
        @Schema(description = "최적 요일", example = "[\"화\", \"수\"]")
        private List<String> bestDays;
    }
}
