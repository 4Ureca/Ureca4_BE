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
@Schema(description = "아웃바운드 카테고리별 전환율")
public class OutboundConversionResponse {

    @Schema(description = "조회 시작일시")
    private LocalDateTime startAt;

    @Schema(description = "조회 종료일시")
    private LocalDateTime endAt;

    @Schema(description = "카테고리별 전환율 목록")
    private List<CategoryConversion> categories;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryConversion {
        @Schema(description = "카테고리 코드", example = "M_OTB_01")
        private String categoryCode;
        @Schema(description = "카테고리명", example = "재약정 권유")
        private String categoryName;
        @Schema(description = "전환 성공 건수")
        private Integer convertedCount;
        @Schema(description = "총 발신 건수")
        private Integer totalCount;
        @Schema(description = "전환율 (%)")
        private Double conversionRate;
    }
}
