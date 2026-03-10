package com.uplus.crm.domain.analysis.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.analysis.dto.*;
import com.uplus.crm.domain.analysis.service.DailyReportService;
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

@WebMvcTest(DailyReportController.class)
@Import(SecurityConfig.class)
class DailyReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DailyReportService dailyReportService;
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

    // ── GET /customer-risk ──────────────────────────────────────

    @Test
    @DisplayName("일별 고객 위험 조회 성공")
    void getCustomerRisk_200() throws Exception {
        mockAuth();
        given(dailyReportService.getCustomerRisk(eq(DATE))).willReturn(
                CustomerRiskResponse.builder()
                        .startAt(LocalDateTime.of(2025, 1, 15, 0, 0))
                        .fraudSuspect(5).totalRiskCount(10).build());

        mockMvc.perform(get("/analysis/admin/daily/customer-risk")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fraudSuspect").value(5))
                .andExpect(jsonPath("$.totalRiskCount").value(10));
    }

    @Test
    @DisplayName("일별 고객 위험 데이터 없음 시 204 반환")
    void getCustomerRisk_204() throws Exception {
        mockAuth();
        given(dailyReportService.getCustomerRisk(eq(DATE))).willReturn(null);

        mockMvc.perform(get("/analysis/admin/daily/customer-risk")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isNoContent());
    }

    // ── GET /customer-risk/compare ──────────────────────────────

    @Test
    @DisplayName("일별 고객 위험 비교 조회 성공")
    void compareCustomerRisk_200() throws Exception {
        mockAuth();
        given(dailyReportService.compareCustomerRisk(
                eq(LocalDate.of(2025, 1, 15)),
                eq(LocalDate.of(2025, 1, 10)))).willReturn(
                CustomerRiskCompareResponse.builder()
                        .surgeDetected(false).build());

        mockMvc.perform(get("/analysis/admin/daily/customer-risk/compare")
                        .param("baseDate", "2025-01-15")
                        .param("compareDate", "2025-01-10")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surgeDetected").value(false));
    }

    @Test
    @DisplayName("일별 고객 위험 비교 데이터 없음 시 204 반환")
    void compareCustomerRisk_204() throws Exception {
        mockAuth();
        given(dailyReportService.compareCustomerRisk(
                eq(LocalDate.of(2025, 1, 15)),
                eq(LocalDate.of(2025, 1, 10)))).willReturn(null);

        mockMvc.perform(get("/analysis/admin/daily/customer-risk/compare")
                        .param("baseDate", "2025-01-15")
                        .param("compareDate", "2025-01-10")
                        .header("Authorization", AUTH))
                .andExpect(status().isNoContent());
    }

    // ── GET /time-slot-trend ────────────────────────────────────

    @Test
    @DisplayName("시간대별 트렌드 조회 성공")
    void getTimeSlotTrend_200() throws Exception {
        mockAuth();
        given(dailyReportService.getTimeSlotTrend(eq(DATE), any())).willReturn(
                Optional.of(TimeSlotTrendResponse.builder().date("2025-01-15").build()));

        mockMvc.perform(get("/analysis/admin/daily/time-slot-trend")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-01-15"));
    }

    // ── GET /category-summary ───────────────────────────────────

    @Test
    @DisplayName("카테고리 요약 조회 성공")
    void getCategorySummary_200() throws Exception {
        mockAuth();
        given(dailyReportService.getCategorySummary(eq(DATE), any())).willReturn(
                Optional.of(CategorySummaryResponse.builder().date("2025-01-15").build()));

        mockMvc.perform(get("/analysis/admin/daily/category-summary")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-01-15"));
    }

    // ── GET /keywords/top ───────────────────────────────────────

    @Test
    @DisplayName("일별 키워드 랭킹 조회 성공")
    void getKeywordRanking_200() throws Exception {
        mockAuth();
        given(dailyReportService.getKeywordRanking(eq(DATE), any())).willReturn(
                Optional.of(KeywordRankingResponse.builder().date("2025-01-15").build()));

        mockMvc.perform(get("/analysis/admin/daily/keywords/top")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-01-15"));
    }

    // ── GET /keywords/customer-types ────────────────────────────

    @Test
    @DisplayName("일별 고객유형별 키워드 조회 성공")
    void getDailyCustomerTypeKeywords_200() throws Exception {
        mockAuth();
        given(dailyReportService.getDailyCustomerTypeKeywords(eq(DATE))).willReturn(
                Optional.of(KeywordAnalysisResponse.builder()
                        .startDate("2025-01-15").endDate("2025-01-15")
                        .byCustomerType(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/daily/keywords/customer-types")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-15"));
    }

    // ── GET /performance ────────────────────────────────────────

    @Test
    @DisplayName("일별 성과 요약 조회 성공")
    void getPerformanceSummary_200() throws Exception {
        mockAuth();
        given(dailyReportService.getDailyPerformanceSummary(eq(DATE))).willReturn(
                Optional.of(PerformanceSummaryResponse.builder()
                        .totalConsultCount(100).avgConsultCountPerAgent(25.0).build()));

        mockMvc.perform(get("/analysis/admin/daily/performance")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConsultCount").value(100))
                .andExpect(jsonPath("$.avgConsultCountPerAgent").value(25.0));
    }

    // ── GET /agent-ranking ──────────────────────────────────────

    @Test
    @DisplayName("일별 상담사 랭킹 조회 성공")
    void getAgentRanking_200() throws Exception {
        mockAuth();
        given(dailyReportService.getDailyAgentRanking(eq(DATE))).willReturn(
                Optional.of(AgentRankingResponse.builder()
                        .startDate("2025-01-15").endDate("2025-01-15")
                        .agents(List.of()).build()));

        mockMvc.perform(get("/analysis/admin/daily/agent-ranking")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2025-01-15"));
    }
}
