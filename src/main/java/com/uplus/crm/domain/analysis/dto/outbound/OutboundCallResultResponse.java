package com.uplus.crm.domain.analysis.dto.outbound;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "아웃바운드 발신 결과 분포 + 거절 사유")
public class OutboundCallResultResponse {

    @Schema(description = "발신 결과 분포")
    private Distribution distribution;

    @Schema(description = "거절 사유 목록 (건수 내림차순)")
    private List<RejectReason> rejectReasons;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        @Schema(description = "전환 성공")
        private ResultCount converted;
        @Schema(description = "거절")
        private ResultCount rejected;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultCount {
        @Schema(description = "건수")
        private Integer count;
        @Schema(description = "비율 (%)")
        private Double rate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectReason {
        @Schema(description = "거절 사유 코드", example = "COST")
        private String code;
        @Schema(description = "건수")
        private Integer count;
        @Schema(description = "비율 (%)")
        private Double rate;
        @Schema(description = "순위")
        private Integer rank;
    }
}
