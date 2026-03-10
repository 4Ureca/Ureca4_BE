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
import com.uplus.crm.domain.analysis.service.MonthlyReportService;
import com.uplus.crm.domain.analysis.service.PerformanceReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@WebMvcTest(MonthlyReportController.class)
@Import(SecurityConfig.class)
class MonthlyReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PerformanceReportService performanceReportService;
    @MockitoBean MonthlyReportService monthlyReportService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    private static final String AUTH = "Bearer mock-token";
    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);

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

    // ── 성과/순위 (PerformanceReportService) ────────────────────

    @Test
    @DisplayName("월별 성과 요약 조회 성공")
    void getPerformanceSummary_200() throws Exception {
        mockAuth();
        given(performanceReportService.getMonthlyPerformanceSummary(eq(DATE))).willReturn(
                Optional.of(PerformanceSummaryResponse.builder().totalConsultCount(500).build()));

        mockMvc.perform(get("/analysis/admin/monthly/performance")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultCount").value(500));
    }

    @Test
    @DisplayName("월별 상담사 랭킹 조회 성공")
    void getAgentRanking_200() throws Exception {
        mockAuth();
        given(performanceReportService.getMonthlyAgentRanking(eq(DATE))).willReturn(
                Optional.of(AgentRankingResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .agents(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/monthly/agent-ranking")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }

    // ── 고객 특이사항 (MonthlyReportService) ────────────────────

    @Test
    @DisplayName("월별 고객 위험 조회 성공")
    void getMonthlyCustomerRisk_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyCustomerRisk(eq(DATE))).willReturn(
                CustomerRiskResponse.builder()
                        .startAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                        .fraudSuspect(10).totalRiskCount(30).build());

        mockMvc.perform(get("/analysis/admin/monthly/customer-risk")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fraudSuspect").value(10));
    }

    // ── 키워드 분리 (MonthlyReportService) ──────────────────────

    @Test
    @DisplayName("월별 TOP 키워드 조회 성공")
    void getTopKeywords_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyKeywordAnalysis(eq(DATE))).willReturn(
                KeywordAnalysisResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .topKeywords(List.of(KeywordAnalysisResponse.TopKeyword.builder()
                                .keyword("요금").count(100).build()))
                        .build());

        mockMvc.perform(get("/analysis/admin/monthly/keywords/top")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topKeywords[0].keyword").value("요금"));
    }

    @Test
    @DisplayName("월별 장기 키워드 조회 성공")
    void getLongTermKeywords_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyKeywordAnalysis(eq(DATE))).willReturn(
                KeywordAnalysisResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .longTermTopKeywords(List.of(KeywordAnalysisResponse.LongTermKeyword.builder()
                                .keyword("해지").build()))
                        .build());

        mockMvc.perform(get("/analysis/admin/monthly/keywords/long-term")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longTermTopKeywords[0].keyword").value("해지"));
    }

    @Test
    @DisplayName("월별 고객유형별 키워드 조회 성공")
    void getCustomerTypeKeywords_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyKeywordAnalysis(eq(DATE))).willReturn(
                KeywordAnalysisResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .byCustomerType(List.of()).build());

        mockMvc.perform(get("/analysis/admin/monthly/keywords/customer-types")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }

    // ── 구독상품 분리 (PerformanceReportService) ────────────────

    @Test
    @DisplayName("월별 구독 상품 조회 성공")
    void getSubscriptionProducts_200() throws Exception {
        mockAuth();
        given(performanceReportService.getMonthlySubscription(eq(DATE))).willReturn(
                Optional.of(SubscriptionAnalysisResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .newSubscriptions(List.of()).canceledSubscriptions(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/monthly/subscription/products")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }

    @Test
    @DisplayName("월별 연령대별 구독 조회 성공")
    void getSubscriptionAgeGroups_200() throws Exception {
        mockAuth();
        given(performanceReportService.getMonthlySubscription(eq(DATE))).willReturn(
                Optional.of(SubscriptionAnalysisResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .byAgeGroup(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/monthly/subscription/age-groups")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }

    // ── 해지방어 분리 (MonthlyReportService) ────────────────────

    @Test
    @DisplayName("월별 해지방어 요약 조회 성공")
    void getChurnDefenseSummary_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyChurnDefenseAnalysis(eq(DATE))).willReturn(
                ChurnDefenseResponse.builder()
                        .totalAttempts(85).successCount(52)
                        .successRate(61.2).avgDurationSec(520).build());

        mockMvc.perform(get("/analysis/admin/monthly/churn-defense/summary")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttempts").value(85));
    }

    @Test
    @DisplayName("월별 해지방어 불만사유 조회 성공")
    void getChurnDefenseComplaintReasons_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyChurnDefenseAnalysis(eq(DATE))).willReturn(
                ChurnDefenseResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .complaintReasons(List.of(ChurnDefenseResponse.ComplaintReason.builder()
                                .reason("요금 불만").attempts(30).build())).build());

        mockMvc.perform(get("/analysis/admin/monthly/churn-defense/complaint-reasons")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complaintReasons[0].reason").value("요금 불만"));
    }

    @Test
    @DisplayName("월별 해지방어 고객유형 조회 성공")
    void getChurnDefenseCustomerTypes_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyChurnDefenseAnalysis(eq(DATE))).willReturn(
                ChurnDefenseResponse.builder()
                        .startDate("2025-01-01").endDate("2025-01-31")
                        .byCustomerType(List.of()).build());

        mockMvc.perform(get("/analysis/admin/monthly/churn-defense/customer-types")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-01"));
    }

    @Test
    @DisplayName("월별 해지방어 조치별 조회 성공")
    void getChurnDefenseActions_200() throws Exception {
        mockAuth();
        given(monthlyReportService.getMonthlyChurnDefenseAnalysis(eq(DATE))).willReturn(
                ChurnDefenseResponse.builder()
                        .byAction(List.of(ChurnDefenseResponse.ActionDefense.builder()
                                .action("요금할인").attempts(25).successRate(72.0).build())).build());

        mockMvc.perform(get("/analysis/admin/monthly/churn-defense/actions")
                        .param("date", "2025-01-15").header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byAction[0].action").value("요금할인"));
    }
}
