package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentReportResponse;
import com.uplus.crm.domain.analysis.service.AgentReportService;
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

/**
 * 상담사 개인 리포트 조회 API
 *
 * 배치가 생성한 daily/weekly/monthly_agent_report_snapshot에서
 * 상담사별 성과 + 응대 품질 분석 데이터를 조회합니다.
 */
@Tag(name = "report_snapshot", description = "일별/주별/월별 분석 리포트 조회 API (관리자 전용)")
@RestController
@RequestMapping("/analysis/agent")
@RequiredArgsConstructor
public class AgentReportController {

    private final AgentReportService agentReportService;

    @Operation(
            summary = "일별 상담사 리포트 조회",
            description = """
                daily_agent_report_snapshot에서 해당 상담사의 일별 리포트를 조회합니다.

                포함 데이터:
                - 상담 처리 건수, 평균 소요 시간, 고객 만족도
                - 처리 카테고리 순위
                - 응대 품질 분석 (공감/사과/마무리/친절/신속/정확/대기안내 + 종합점수)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 스냅샷 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/daily")
    public ResponseEntity<AgentReportResponse> getDailyReport(
            @Parameter(description = "상담사 ID", required = true, example = "1")
            @RequestParam Long agentId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

        return agentReportService.getDailyReport(agentId, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "주별 상담사 리포트 조회",
            description = """
                weekly_agent_report_snapshot에서 해당 상담사의 주별 리포트를 조회합니다.
                date가 포함되는 주간 스냅샷(startAt <= date <= endAt)을 찾습니다.

                포함 데이터:
                - 상담 처리 건수, 평균 소요 시간, 고객 만족도
                - 처리 카테고리 순위
                - 응대 품질 분석 (일별 데이터 가중 평균)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 주간 스냅샷 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/weekly")
    public ResponseEntity<AgentReportResponse> getWeeklyReport(
            @Parameter(description = "상담사 ID", required = true, example = "1")
            @RequestParam Long agentId,
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 날짜가 포함된 주간 스냅샷 조회", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return agentReportService.getWeeklyReport(agentId, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "월별 상담사 리포트 조회",
            description = """
                monthly_agent_report_snapshot에서 해당 상담사의 월별 리포트를 조회합니다.
                date가 포함되는 월간 스냅샷(startAt <= date <= endAt)을 찾습니다.

                포함 데이터:
                - 상담 처리 건수, 평균 소요 시간, 고객 만족도
                - 처리 카테고리 순위
                - 응대 품질 분석 (일별 데이터 가중 평균)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 월간 스냅샷 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/monthly")
    public ResponseEntity<AgentReportResponse> getMonthlyReport(
            @Parameter(description = "상담사 ID", required = true, example = "1")
            @RequestParam Long agentId,
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 날짜가 포함된 월간 스냅샷 조회", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        return agentReportService.getMonthlyReport(agentId, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
