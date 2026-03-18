package com.uplus.crm.domain.analysis.controller.agent;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
//import com.uplus.crm.domain.analysis.dto.agent.AgentQualityResponse;
import com.uplus.crm.domain.analysis.dto.agent.AgentSatisfactionResponse;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.analysis.service.agent.AgentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

@Tag(name = "agent_report", description = "상담사 개인 분석 리포트 조회 API (상담사/관리자 공용)")
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
          "date 미지정 시 전일(어제)/전주/전월 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "상담사 성과 조회 성공",
          content = @Content(schema = @Schema(implementation = AgentMetricsResponse.class))),
      @ApiResponse(responseCode = "204", description = "해당 날짜에 대한 리포트 데이터가 존재하지 않음"),
      @ApiResponse(responseCode = "400", description = "상담사 ID 누락 (관리자 조회 시 필수)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/metrics")
  public ResponseEntity<AgentMetricsResponse> getMetrics(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @Parameter(description = "조회 대상 상담사 ID. **[관리자]** 필수 입력. **[상담사]** 본인 ID 외 입력 불가 (입력해도 본인 것만 조회됨)", example = "11")
      @RequestParam(required = false) Integer targetEmpId, // 관리자가 선택한 상담사 ID
      @AuthenticationPrincipal CustomUserDetails userDetails) { // 로그인 정보에서 empId 추출

    // 1. 최종 조회할 empId 결정
    Integer finalEmpId;
    if (userDetails.isAdmin()) {
      if (targetEmpId == null) {
        throw new BusinessException(ErrorCode.MISSING_TARGET_EMPID); // 400 에러
      }
      finalEmpId = targetEmpId;
    } else {
      finalEmpId = userDetails.getEmpId();
    }

    LocalDate targetDate = getDefaultTargetDate(period, date);

    // 3. 서비스 호출 및 결과 확인
    AgentMetricsResponse result = agentReportService.getMetrics(period, finalEmpId, targetDate);

    // [핵심 수정 부분] 결과가 null이면 204 No Content 반환
    if (result == null) {
      return ResponseEntity.noContent().build(); // HTTP 204 응답
    }

    // 데이터가 있으면 200 OK와 함께 데이터 반환
    return ResponseEntity.ok(result);
  }

  /**
   * 2. 처리 카테고리 건수 및 순위 조회
   * GET /api/analysis/agent/{period}/categories
   */
  @Operation(
      summary = "상담사 카테고리 순위 조회",
      description = "특정 기간(daily, weekly, monthly) 동안 상담사가 처리한 상담 카테고리 순위를 조회합니다. " +
          "date 미지정 시 전일(어제)/전주/전월 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "상담사 카테고리 순위 조회 성공",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryRankingDto.class)))),
      @ApiResponse(responseCode = "204", description = "해당 날짜에 대한 리포트 데이터가 존재하지 않음"),
      @ApiResponse(responseCode = "400", description = "상담사 ID 누락 (관리자 조회 시 필수)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/categories")
  public ResponseEntity<List<CategoryRankingDto>> getCategories(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @Parameter(description = "조회 대상 상담사 ID. **[관리자]** 필수 입력. **[상담사]** 본인 ID 외 입력 불가 (입력해도 본인 것만 조회됨)", example = "11")
      @RequestParam(required = false) Integer targetEmpId, // 관리자가 선택한 상담사 ID
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    // 1. 최종 조회할 empId 결정
    // 관리자이면서 targetEmpId가 들어온 경우에만 해당 ID를 사용, 그 외엔 본인 ID
    Integer finalEmpId;
    if (userDetails.isAdmin()) {
      if (targetEmpId == null) {
        throw new BusinessException(ErrorCode.MISSING_TARGET_EMPID); // 400 에러
      }
      finalEmpId = targetEmpId;
    } else {
      finalEmpId = userDetails.getEmpId();
    }

    LocalDate targetDate = getDefaultTargetDate(period, date);

    // 3. 서비스 호출
    List<CategoryRankingDto> result = agentReportService.getCategories(period, finalEmpId, targetDate);

    // [핵심 수정 부분] 리스트가 비어있다면 204 No Content 반환
    if (result == null || result.isEmpty()) {
      return ResponseEntity.noContent().build(); // HTTP 204 응답
    }

    // 데이터가 있으면 200 OK와 함께 리스트 반환
    return ResponseEntity.ok(result);
  }

  /**
   * 3. 고객 만족도 조회
   * GET /api/analysis/agent/{period}/satisfaction
   */
  @Operation(
      summary = "고객 만족도 조회",
      description = "특정 기간(daily, weekly, monthly) 동안의 고객 만족도를 조회합니다. " +
          "date 미지정 시 전일(어제)/전주/전월 기준으로 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "고객 만족도 조회 성공",
          content = @Content(schema = @Schema(implementation = AgentSatisfactionResponse.class))),
      @ApiResponse(responseCode = "204", description = "해당 날짜에 대한 리포트 데이터가 존재하지 않음"),
      @ApiResponse(responseCode = "400", description = "상담사 ID 누락 (관리자 조회 시 필수)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{period}/satisfaction")
  public ResponseEntity<AgentSatisfactionResponse> getSatisfaction(
      @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
      @PathVariable String period,
      @Parameter(description = "조회 기준 날짜 (ISO 형식: YYYY-MM-DD)", example = "2025-01-15")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @Parameter(description = "조회 대상 상담사 ID. **[관리자]** 필수 입력. **[상담사]** 본인 ID 외 입력 불가 (입력해도 본인 것만 조회됨)", example = "11")
      @RequestParam(required = false) Integer targetEmpId, // 관리자가 선택한 상담사 ID
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    // 1. 최종 조회할 empId 결정
    Integer finalEmpId;
    if (userDetails.isAdmin()) {
      if (targetEmpId == null) {
        throw new BusinessException(ErrorCode.MISSING_TARGET_EMPID); // 400 에러
      }
      finalEmpId = targetEmpId;
    } else {
      finalEmpId = userDetails.getEmpId();
    }

    LocalDate targetDate = getDefaultTargetDate(period, date);

    // 3. 서비스 호출
    AgentSatisfactionResponse result = agentReportService.getSatisfaction(period, finalEmpId, targetDate);

    // [수정] 결과가 null이면 204 No Content 반환
    if (result == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(result);
  }

  private LocalDate getDefaultTargetDate(String period, LocalDate date) {
    if (date != null) return date;

    return switch (period.toLowerCase()) {
      case "weekly" -> LocalDate.now().minusWeeks(1);  // 1주일 전 (지난주 데이터 포함 날짜)
      case "monthly" -> LocalDate.now().minusMonths(1); // 1개월 전 (지난달 데이터 포함 날짜)
      default -> LocalDate.now().minusDays(1);         // 기본값: 어제
    };
  }

}