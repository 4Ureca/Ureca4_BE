package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "고객 특이사항 집계 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRiskResponse {

    @Schema(description = "집계 시작 시각", example = "2025-01-15T00:00:00")
    private LocalDateTime startAt;

    @Schema(description = "집계 종료 시각", example = "2025-01-15T23:59:59")
    private LocalDateTime endAt;

    @Schema(description = "사기 의심 건수", example = "2")
    private int fraudSuspect;

    @Schema(description = "악성 민원 건수", example = "6")
    private int maliciousComplaint;

    @Schema(description = "정책 악용 건수", example = "1")
    private int policyAbuse;

    @Schema(description = "과도한 보상 요구 건수", example = "4")
    private int excessiveCompensation;

    @Schema(description = "반복 민원 건수", example = "10")
    private int repeatedComplaint;

    @Schema(description = "피싱 피해 건수", example = "1")
    private int phishingVictim;

    @Schema(description = "해지 위험 건수", example = "3")
    private int churnRisk;

    @Schema(description = "위험 플래그 총 건수 (7종 합계)", example = "27")
    private int totalRiskCount;
    
    @Schema(description = "전일 대비 급증 경고 (전일 데이터 없으면 null)")
    private SurgeAlert surgeAlerts;
    
    @Schema(description = "전일 대비 급증 경고")
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SurgeAlert {
        private int previousTotalRiskCount;
        private double changeRate;
        private boolean surgeDetected;
        private List<String> surgeTypes;
    }
}