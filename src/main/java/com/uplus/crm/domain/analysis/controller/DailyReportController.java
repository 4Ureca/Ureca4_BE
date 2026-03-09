package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.CategorySummaryResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.KeywordRankingResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.TimeSlotTrendResponse;
import com.uplus.crm.domain.analysis.service.DailyReportService;
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
@RequestMapping("/analysis/admin/daily")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @Operation(
            summary = "고객 특이사항 조회",
            description = "daily_report_snapshot의 customerRiskAnalysis 섹션을 조회합니다. "
                    + "매일 02:00 배치가 생성한 스냅샷 기반이며, 실시간 집계가 아닙니다. "
                    + "전일 스냅샷과 비교하여 급증 경고(surgeAlerts)를 API 조회 시점에 계산합니다. "
                    + "date 미지정 시 전일(어제) 기준으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk")
    public ResponseEntity<CustomerRiskResponse> getCustomerRisk(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        CustomerRiskResponse response = dailyReportService.getCustomerRisk(targetDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 특이사항 기간 비교",
            description = "두 날짜의 daily_report_snapshot을 비교하여 유형별 증감율과 급증 여부를 계산합니다. "
                    + "관리자가 직접 기준일/비교일을 선택하여 상세 분석할 때 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비교 성공"),
            @ApiResponse(responseCode = "204", description = "기준일 또는 비교일 스냅샷 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk/compare")
    public ResponseEntity<CustomerRiskCompareResponse> compareCustomerRisk(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate baseDate,

            @Parameter(description = "비교 날짜 (yyyy-MM-dd)", example = "2025-01-10")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate compareDate) {

        CustomerRiskCompareResponse response = dailyReportService.compareCustomerRisk(baseDate, compareDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-slot-trend")
    @Operation(summary = "시간대별 이슈 트렌드", description = "슬롯별 상담 건수/평균시간/카테고리 분포. date 미지정 시 전일 기준.")
    public ResponseEntity<TimeSlotTrendResponse> getTimeSlotTrend(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (예: 09-12). 미지정 시 전체 슬롯", example = "09-12")
            @RequestParam(required = false) String slot) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        return dailyReportService.getTimeSlotTrend(targetDate, slot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/category-summary")
    @Operation(summary = "카테고리별 빈도", description = "슬롯별 또는 전체 카테고리 빈도. date 미지정 시 전일 기준.")
    public ResponseEntity<CategorySummaryResponse> getCategorySummary(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (예: 09-12). 미지정 시 전체 슬롯", example = "09-12")
            @RequestParam(required = false) String slot) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        return dailyReportService.getCategorySummary(targetDate, slot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/keywords/top")
    @Operation(summary = "키워드 빈도 순위", description = "슬롯별 또는 전체 키워드 순위/증감율/신규 진입. date 미지정 시 전일 기준.")
    public ResponseEntity<KeywordRankingResponse> getKeywordRanking(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (예: 09-12). 미지정 시 전체 슬롯", example = "09-12")
            @RequestParam(required = false) String slot) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        return dailyReportService.getKeywordRanking(targetDate, slot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/keywords/customer-types")
    @Operation(summary = "고객 유형별 키워드", description = "고객 등급별 빈출 키워드를 조회합니다. date 미지정 시 전일 기준.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 데이터 없음", content = @Content)
    })
    public ResponseEntity<KeywordAnalysisResponse> getDailyCustomerTypeKeywords(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        return dailyReportService.getDailyCustomerTypeKeywords(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "일별 전체 상담 성과 요약",
            description = "daily_agent_report_snapshot에서 해당 일자의 상담사 데이터를 집계하여 성과 요약을 제공합니다. "
                    + "총 상담 수, 상담사별 평균 처리 건수, 평균 소요 시간, 평균 고객 만족도를 제공합니다. "
                    + "date 미지정 시 전일(어제) 기준으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 상담사 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/performance")
    public ResponseEntity<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

        return dailyReportService.getDailyPerformanceSummary(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "일별 상담사 성과 순위 (TOP 10)",
            description = "daily_agent_report_snapshot에서 해당 일자의 상담사별 종합 점수를 계산하여 순위를 제공합니다. "
                    + "종합 점수: 처리건수(25%) + 소요시간(15%) + 응대품질(30%) + 고객만족도(30%). "
                    + "date 미지정 시 전일(어제) 기준으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 상담사 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agent-ranking")
    public ResponseEntity<AgentRankingResponse> getAgentRanking(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

        return dailyReportService.getDailyAgentRanking(targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}