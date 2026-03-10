package com.uplus.crm.domain.analysis.controller.agent;

import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
//import com.uplus.crm.domain.analysis.dto.agent.AgentQualityResponse;
import com.uplus.crm.domain.analysis.dto.agent.AgentSatisfactionResponse;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.analysis.service.agent.AgentReportService;
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
  @GetMapping("/{period}/metrics")
  public ResponseEntity<AgentMetricsResponse> getMetrics(
      @PathVariable String period,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) { // 로그인 정보에서 empId 추출

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getMetrics(period, empId, targetDate));
  }

  /**
   * 2. 처리 카테고리 건수 및 순위 조회
   * GET /api/analysis/agent/{period}/categories
   */
  @GetMapping("/{period}/categories")
  public ResponseEntity<List<CategoryRankingDto>> getCategories(
      @PathVariable String period,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getCategories(period, empId, targetDate));
  }

  /**
   * 3. 고객 만족도 조회
   * GET /api/analysis/agent/{period}/satisfaction
   */
  @GetMapping("/{period}/satisfaction")
  public ResponseEntity<AgentSatisfactionResponse> getSatisfaction(
      @PathVariable String period,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal(expression = "empId") Integer empId) {

    LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

    return ResponseEntity.ok(agentReportService.getSatisfaction(period, empId, targetDate));
  }
}