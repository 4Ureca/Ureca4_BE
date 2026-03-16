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
@Schema(description = "아웃바운드 캠페인 성과 현황")
public class OutboundCampaignResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "캠페인별 성과 목록")
    private List<CampaignDetail> campaigns;

    @Schema(description = "전체 합계")
    private CampaignTotal total;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignDetail {
        @Schema(description = "카테고리 코드", example = "M_OTB_01")
        private String categoryCode;
        @Schema(description = "카테고리명", example = "재약정 권유")
        private String categoryName;
        @Schema(description = "캠페인 활성 여부")
        private Boolean isActive;
        @Schema(description = "총 발신 건수")
        private Integer totalCount;
        @Schema(description = "전환 성공 건수")
        private Integer convertedCount;
        @Schema(description = "전환율 (%)")
        private Double conversionRate;
        @Schema(description = "평균 통화 시간 (초)")
        private Double avgDurationSec;
        @Schema(description = "평균 만족도 (0~5)")
        private Double avgSatisfiedScore;
        @Schema(description = "예상 매출")
        private Long estimatedRevenue;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignTotal {
        private Integer totalCount;
        private Double conversionRate;
        private Double avgDurationSec;
        private Double avgSatisfiedScore;
        private Long estimatedRevenue;
    }
}
