package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.ChurnDefenseResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.SubscriptionAnalysisResponse;
import com.uplus.crm.domain.analysis.service.MonthlyReportService;
import com.uplus.crm.domain.analysis.service.PerformanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "report_snapshot", description = "일별/주별/월별 분석 리포트 조회 API (관리자 전용)")
@RestController
@RequestMapping("/analysis/admin/monthly")
@RequiredArgsConstructor
public class MonthlyReportController {

    private final PerformanceReportService performanceReportService;
    private final MonthlyReportService monthlyReportService;

    // ==================== 성과/순위 ====================

    @Operation(summary = "월간 전체 상담 성과 요약",
            description = "monthly_report_snapshot에서 월간 성과 요약을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월간 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance")
    public ResponseEntity<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getMonthlyPerformanceSummary(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "월간 상담사 성과 순위 (TOP 10)",
            description = "monthly_report_snapshot에서 상담사별 성과 순위를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월간 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agent-ranking")
    public ResponseEntity<AgentRankingResponse> getAgentRanking(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getMonthlyAgentRanking(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 고객 특이사항 ====================

    @Operation(summary = "월별 고객 특이사항 조회",
            description = "monthly_report_snapshot의 customerRiskAnalysis를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk")
    public ResponseEntity<CustomerRiskResponse> getMonthlyCustomerRisk(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        CustomerRiskResponse response = monthlyReportService.getMonthlyCustomerRisk(targetDate);
        if (response == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }

    // ==================== 키워드 분리 ====================

    @GetMapping("/keywords/top")
    @Operation(summary = "월간 키워드 빈도 순위", description = "키워드 빈도 순위(TOP 20)와 증감율을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getTopKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(KeywordAnalysisResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .topKeywords(full.getTopKeywords())
                .build());
    }

    @GetMapping("/keywords/long-term")
    @Operation(summary = "월간 장기 상위 유지 키워드", description = "장기간 상위권을 유지하는 키워드를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getLongTermKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(KeywordAnalysisResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .longTermTopKeywords(full.getLongTermTopKeywords())
                .build());
    }

    @GetMapping("/keywords/customer-types")
    @Operation(summary = "월간 고객 유형별 키워드", description = "고객 등급별 빈출 키워드를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getCustomerTypeKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        KeywordAnalysisResponse full = monthlyReportService.getMonthlyKeywordAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(KeywordAnalysisResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .byCustomerType(full.getByCustomerType())
                .build());
    }

    // ==================== 구독상품 분리 ====================

    @GetMapping("/subscription/products")
    @Operation(summary = "월간 상품별 신규가입/해지", description = "신규 가입 및 해지 상위 상품(TOP 6)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionProducts(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        return performanceReportService.getMonthlySubscription(targetDate)
                .map(r -> ResponseEntity.ok(SubscriptionAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .newSubscriptions(r.getNewSubscriptions())
                        .canceledSubscriptions(r.getCanceledSubscriptions())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/subscription/age-groups")
    @Operation(summary = "월간 연령대별 상품 선호도", description = "연령대별 선호 상품(TOP 3)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionAgeGroups(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        return performanceReportService.getMonthlySubscription(targetDate)
                .map(r -> ResponseEntity.ok(SubscriptionAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .byAgeGroup(r.getByAgeGroup())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 해지방어 분리 ====================

    @GetMapping("/churn-defense/summary")
    @Operation(summary = "월간 해지방어 요약", description = "해지방어 시도 건수, 성공 건수, 성공률, 평균 소요시간을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseSummary(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .totalAttempts(full.getTotalAttempts())
                .successCount(full.getSuccessCount())
                .successRate(full.getSuccessRate())
                .avgDurationSec(full.getAvgDurationSec())
                .build());
    }

    @GetMapping("/churn-defense/complaint-reasons")
    @Operation(summary = "월간 불만 사유별 방어율", description = "고객 불만 사유별 해지방어 시도/성공/성공률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseComplaintReasons(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .complaintReasons(full.getComplaintReasons())
                .build());
    }

    @GetMapping("/churn-defense/customer-types")
    @Operation(summary = "월간 고객 유형별 해지 분석", description = "고객 유형(연령+성별)별 해지 시도/방어 성공률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseCustomerTypes(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .byCustomerType(full.getByCustomerType())
                .build());
    }

    @GetMapping("/churn-defense/actions")
    @Operation(summary = "월간 상담사 액션 분석", description = "방어 액션별 시도 건수/성공률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseActions(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        ChurnDefenseResponse full = monthlyReportService.getMonthlyChurnDefenseAnalysis(targetDate);
        if (full == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ChurnDefenseResponse.builder()
                .startDate(full.getStartDate())
                .endDate(full.getEndDate())
                .byAction(full.getByAction())
                .build());
    }
}
