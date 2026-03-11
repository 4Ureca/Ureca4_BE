package com.uplus.crm.domain.analysis.controller.agent;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
//import com.uplus.crm.domain.analysis.dto.agent.AgentQualityResponse;
import com.uplus.crm.domain.analysis.dto.agent.AgentSatisfactionResponse;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.analysis.service.agent.AgentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "agent_report", description = "상담사 개인 분석 리포트 조회 API")
@RestController
@RequestMapping("/api/analysis/agent")
@RequiredArgsConstructor
public class AgentReportController {

  private final AgentReportService agentReportService;

  /**
   * 1. 전체 성과 (Metrics) 조회
   * GET /api/analysis/agent/{period}/metrics
   */
  @Operation(
      summary = "상담사 성과 조회",
      description = "특정 기간(daily, weekly, monthly) 동안 상담사의 상담 건수, 만족도 등을 조회합니다 " +
          "date 미지정 시 전일(어제) 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "요청한 날짜에 해당하는 리포트 데이터가 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/metrics")
  public ResponseEntity<AgentMetricsResponse> getMetrics(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) { // 로그인 정보에서 empId 추출

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getMetrics(period, empId, targetDate));
  }

  /**
   * 2. 처리 카테고리 건수 및 순위 조회
   * GET /api/analysis/agent/{period}/categories
   */
  @Operation(
      summary = "상담사 카테고리 순위 조회",
      description = "특정 기간(daily, weekly, monthly) 동안 상담사가 처리한 상담 카테고리 순위를 조회합니다. " +
          "date 미지정 시 전일(어제) 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "요청한 날짜에 해당하는 리포트 데이터가 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/categories")
  public ResponseEntity<List<CategoryRankingDto>> getCategories(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getCategories(period, empId, targetDate));
  }

  /**
   * 3. 고객 만족도 조회
   * GET /api/analysis/agent/{period}/satisfaction
   */
  @Operation(
      summary = "고객 만족도 조회",
      description = "특정 기간(daily, weekly, monthly) 동안의 고객 만족도를 조회합니다. " +
          "date 미지정 시 전일(어제) 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "요청한 날짜에 해당하는 리포트 데이터가 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/satisfaction")
  public ResponseEntity<AgentSatisfactionResponse> getSatisfaction(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getSatisfaction(period, empId, targetDate));
  }
}