package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.SubscriptionAnalysisResponse;
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
@RequestMapping("/analysis/admin/weekly")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final PerformanceReportService performanceReportService;

    @Operation(
            summary = "주간 전체 상담 성과 요약",
            description = "weekly_report_snapshot에서 주간 성과 요약을 조회합니다. "
                    + "총 상담 수, 상담사별 평균 처리 건수, 평균 소요 시간, 평균 고객 만족도를 제공합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance")
    public ResponseEntity<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 주간 스냅샷 조회", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklyPerformanceSummary(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "주간 상담사 성과 순위 (TOP 10)",
            description = "weekly_report_snapshot에서 상담사별 성과 순위를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agent-ranking")
    public ResponseEntity<AgentRankingResponse> getAgentRanking(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 주간 스냅샷 조회", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklyAgentRanking(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 키워드 분리 ====================

    @GetMapping("/keywords/top")
    @Operation(summary = "주간 키워드 빈도 순위", description = "키워드 빈도 순위(TOP 20)와 증감율을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getTopKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklyKeywordAnalysis(targetDate)
                .map(r -> ResponseEntity.ok(KeywordAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .topKeywords(r.getTopKeywords())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/keywords/long-term")
    @Operation(summary = "주간 장기 상위 유지 키워드", description = "장기간 상위권을 유지하는 키워드를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getLongTermKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklyKeywordAnalysis(targetDate)
                .map(r -> ResponseEntity.ok(KeywordAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .longTermTopKeywords(r.getLongTermTopKeywords())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/keywords/customer-types")
    @Operation(summary = "주간 고객 유형별 키워드", description = "고객 등급별 빈출 키워드를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getCustomerTypeKeywords(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklyKeywordAnalysis(targetDate)
                .map(r -> ResponseEntity.ok(KeywordAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .byCustomerType(r.getByCustomerType())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 구독상품 분리 ====================

    @GetMapping("/subscription/products")
    @Operation(summary = "주간 상품별 신규가입/해지", description = "신규 가입 및 해지 상위 상품(TOP 6)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionProducts(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklySubscription(targetDate)
                .map(r -> ResponseEntity.ok(SubscriptionAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .newSubscriptions(r.getNewSubscriptions())
                        .canceledSubscriptions(r.getCanceledSubscriptions())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/subscription/age-groups")
    @Operation(summary = "주간 연령대별 상품 선호도", description = "연령대별 선호 상품(TOP 3)을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음", content = @Content)
    })
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionAgeGroups(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return performanceReportService.getWeeklySubscription(targetDate)
                .map(r -> ResponseEntity.ok(SubscriptionAnalysisResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .byAgeGroup(r.getByAgeGroup())
                        .build()))
                .orElse(ResponseEntity.noContent().build());
    }
}
