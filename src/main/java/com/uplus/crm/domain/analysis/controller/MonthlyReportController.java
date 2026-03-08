package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
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
@RequestMapping("/analysis/monthly")
@RequiredArgsConstructor
public class MonthlyReportController {

    private final PerformanceReportService performanceReportService;
    private final MonthlyReportService monthlyReportService;

    @Operation(
            summary = "월간 전체 상담 성과 요약",
            description = "monthly_report_snapshot에서 월간 성과 요약을 조회합니다. "
                    + "총 상담 수, 상담사별 평균 처리 건수, 평균 소요 시간, 평균 고객 만족도를 제공합니다. "
                    + "date 미지정 시 가장 최근 월간 스냅샷을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월간 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance-summary")
    public ResponseEntity<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 날짜가 포함된 월간 스냅샷 조회. 미지정 시 최근 스냅샷",
                    example = "2025-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return performanceReportService.getMonthlyPerformanceSummary(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "월간 상담사 성과 순위 (TOP 10)",
            description = "monthly_report_snapshot에서 상담사별 성과 순위를 조회합니다. "
                    + "처리 건수 내림차순 기준 상위 10명의 성과 데이터를 제공합니다. "
                    + "date 미지정 시 가장 최근 월간 스냅샷을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월간 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agent-ranking")
    public ResponseEntity<AgentRankingResponse> getAgentRanking(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 날짜가 포함된 월간 스냅샷 조회. 미지정 시 최근 스냅샷",
                    example = "2025-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return performanceReportService.getMonthlyAgentRanking(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "월별 고객 특이사항 조회",
            description = "monthly_report_snapshot의 customerRiskAnalysis 섹션을 조회합니다. "
                    + "월별 배치(monthlyAdminReportJob)가 생성한 스냅샷 기반이며, 실시간 집계가 아닙니다. "
                    + "date에 해당 월의 아무 날짜를 넣으면 해당 월 스냅샷을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk")
    public ResponseEntity<CustomerRiskResponse> getMonthlyCustomerRisk(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 해당 월의 아무 날짜", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        CustomerRiskResponse response = monthlyReportService.getMonthlyCustomerRisk(targetDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "월별 키워드 분석 조회",
            description = "monthly_report_snapshot의 keywordSummary 섹션을 조회합니다. "
                    + "키워드 빈도 순위(TOP 20), 증감율, 장기 상위 유지 키워드, 고객 유형별 키워드를 제공합니다. "
                    + "date에 해당 월의 아무 날짜를 넣으면 해당 월 스냅샷을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/keyword-ranking")
    public ResponseEntity<KeywordAnalysisResponse> getMonthlyKeywordAnalysis(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 해당 월의 아무 날짜", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusMonths(1);
        KeywordAnalysisResponse response = monthlyReportService.getMonthlyKeywordAnalysis(targetDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
