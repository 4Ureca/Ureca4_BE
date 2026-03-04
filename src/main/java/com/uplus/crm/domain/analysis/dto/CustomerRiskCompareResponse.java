package com.uplus.crm.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "고객 특이사항 기간 비교 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRiskCompareResponse {

    @Schema(description = "기준 날짜", example = "2025-01-15")
    private LocalDate baseDate;

    @Schema(description = "비교 날짜", example = "2025-01-10")
    private LocalDate compareDate;

    @Schema(description = "기준 날짜 위험 건수")
    private RiskSnapshot base;

    @Schema(description = "비교 날짜 위험 건수")
    private RiskSnapshot compare;

    @Schema(description = "유형별 증감 상세")
    private Map<String, ChangeDetail> changes;

    @Schema(description = "급증 여부 (증감율 50% 이상 또는 유형별 2배 이상)", example = "true")
    private boolean surgeDetected;

    @Schema(description = "급증 유형 코드 목록")
    private List<String> surgeTypes;

    @Schema(description = "특정 날짜의 위험유형별 건수 스냅샷")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskSnapshot {

        @Schema(description = "사기 의심 건수", example = "10")
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
    }

    @Schema(description = "유형별 증감 상세")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeDetail {

        @Schema(description = "건수 차이 (기준 - 비교)", example = "8")
        private int diff;

        @Schema(description = "증감율 (%)", example = "400.0")
        private double changeRate;
    }
}