package com.uplus.crm.domain.analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.analysis.dto.*;
import com.uplus.crm.domain.analysis.service.PerformanceReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebMvcTest(WeeklyReportController.class)
@Import(SecurityConfig.class)
class WeeklyReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PerformanceReportService performanceReportService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    private static final String AUTH = "Bearer mock-token";
    private static final LocalDate DATE = LocalDate.of(2025, 1, 18);

    private void mockAuth() {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(1);
        JobRole role = mock(JobRole.class);
        given(role.getRoleName()).willReturn("관리자");
        EmployeeDetail detail = mock(EmployeeDetail.class);
        given(detail.getJobRole()).willReturn(role);
        Employee employee = mock(Employee.class);
        given(employee.getEmployeeDetail()).willReturn(detail);
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.of(employee));
    }

    // ── GET /performance ────────────────────────────────────────

    @Test
    @DisplayName("주별 성과 요약 조회 성공")
    void getPerformanceSummary_200() throws Exception {
        mockAuth();
        given(performanceReportService.getWeeklyPerformanceSummary(eq(DATE))).willReturn(
                Optional.of(PerformanceSummaryResponse.builder()
                        .totalConsultCount(200).build()));

        mockMvc.perform(get("/analysis/admin/weekly/performance")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultCount").value(200));
    }

    // ── GET /agent-ranking ──────────────────────────────────────

    @Test
    @DisplayName("주별 상담사 랭킹 조회 성공")
    void getAgentRanking_200() throws Exception {
        mockAuth();
        given(performanceReportService.getWeeklyAgentRanking(eq(DATE))).willReturn(
                Optional.of(AgentRankingResponse.builder()
                        .startDate("2025-01-13").endDate("2025-01-18")
                        .agents(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/weekly/agent-ranking")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-13"));
    }

    // ── GET /keywords/top ───────────────────────────────────────

    @Test
    @DisplayName("주별 TOP 키워드 조회 성공")
    void getTopKeywords_200() throws Exception {
        mockAuth();
        KeywordAnalysisResponse full = KeywordAnalysisResponse.builder()
                .startDate("2025-01-13").endDate("2025-01-18")
                .topKeywords(List.of(KeywordAnalysisResponse.TopKeyword.builder()
                        .keyword("요금").count(50).build()))
                .longTermTopKeywords(List.of())
                .byCustomerType(List.of())
                .build();
        given(performanceReportService.getWeeklyKeywordAnalysis(eq(DATE))).willReturn(Optional.of(full));

        mockMvc.perform(get("/analysis/admin/weekly/keywords/top")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topKeywords[0].keyword").value("요금"));
    }

    @Test
    @DisplayName("주별 TOP 키워드 데이터 없음 시 204 반환")
    void getTopKeywords_204() throws Exception {
        mockAuth();
        given(performanceReportService.getWeeklyKeywordAnalysis(eq(DATE))).willReturn(Optional.empty());

        mockMvc.perform(get("/analysis/admin/weekly/keywords/top")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isNoContent());
    }

    // ── GET /keywords/long-term ─────────────────────────────────

    @Test
    @DisplayName("주별 장기 키워드 조회 성공")
    void getLongTermKeywords_200() throws Exception {
        mockAuth();
        KeywordAnalysisResponse full = KeywordAnalysisResponse.builder()
                .startDate("2025-01-13").endDate("2025-01-18")
                .longTermTopKeywords(List.of(KeywordAnalysisResponse.LongTermKeyword.builder()
                        .keyword("해지").build()))
                .build();
        given(performanceReportService.getWeeklyKeywordAnalysis(eq(DATE))).willReturn(Optional.of(full));

        mockMvc.perform(get("/analysis/admin/weekly/keywords/long-term")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longTermTopKeywords[0].keyword").value("해지"));
    }

    // ── GET /keywords/customer-types ────────────────────────────

    @Test
    @DisplayName("주별 고객유형별 키워드 조회 성공")
    void getCustomerTypeKeywords_200() throws Exception {
        mockAuth();
        KeywordAnalysisResponse full = KeywordAnalysisResponse.builder()
                .startDate("2025-01-13").endDate("2025-01-18")
                .byCustomerType(List.of()).build();
        given(performanceReportService.getWeeklyKeywordAnalysis(eq(DATE))).willReturn(Optional.of(full));

        mockMvc.perform(get("/analysis/admin/weekly/keywords/customer-types")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-13"));
    }

    // ── GET /subscription/products ──────────────────────────────

    @Test
    @DisplayName("주별 구독 상품 조회 성공")
    void getSubscriptionProducts_200() throws Exception {
        mockAuth();
        SubscriptionAnalysisResponse full = SubscriptionAnalysisResponse.builder()
                .startDate("2025-01-13").endDate("2025-01-18")
                .newSubscriptions(List.of()).canceledSubscriptions(List.of())
                .build();
        given(performanceReportService.getWeeklySubscription(eq(DATE))).willReturn(Optional.of(full));

        mockMvc.perform(get("/analysis/admin/weekly/subscription/products")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-13"));
    }

    // ── GET /subscription/age-groups ────────────────────────────

    @Test
    @DisplayName("주별 연령대별 구독 조회 성공")
    void getSubscriptionAgeGroups_200() throws Exception {
        mockAuth();
        SubscriptionAnalysisResponse full = SubscriptionAnalysisResponse.builder()
                .startDate("2025-01-13").endDate("2025-01-18")
                .byAgeGroup(List.of()).build();
        given(performanceReportService.getWeeklySubscription(eq(DATE))).willReturn(Optional.of(full));

        mockMvc.perform(get("/analysis/admin/weekly/subscription/age-groups")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-13"));
    }
}
