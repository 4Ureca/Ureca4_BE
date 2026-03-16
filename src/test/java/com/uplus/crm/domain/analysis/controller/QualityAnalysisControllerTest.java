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
import com.uplus.crm.domain.analysis.dto.QualityAnalysisResponse;
import com.uplus.crm.domain.analysis.service.QualityAnalysisService;
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

@WebMvcTest(QualityAnalysisController.class)
@Import(SecurityConfig.class)
class QualityAnalysisControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean QualityAnalysisService qualityAnalysisService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    private static final String AUTH = "Bearer mock-token";

    // 파라미터로 roleName을 받도록 수정
    private void mockAuth(String roleName) {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(1);
        JobRole role = mock(JobRole.class);
        // 고정된 "상담사" 대신 파라미터로 받은 값을 반환
        given(role.getRoleName()).willReturn(roleName);
        EmployeeDetail detail = mock(EmployeeDetail.class);
        given(detail.getJobRole()).willReturn(role);
        Employee employee = mock(Employee.class);
        given(employee.getEmployeeDetail()).willReturn(detail);
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.of(employee));
    }

    private void mockAuth() {
        mockAuth("상담사");
    }

    // ── 일별 ─────────────────────────────────────────────

    @Test
    @DisplayName("일별 응대품질 특정 상담사 조회 성공")
    void getDailyQuality_withAgent_200() throws Exception {
        mockAuth();
        given(qualityAnalysisService.getDailyByAgent(eq(1L), eq(LocalDate.of(2025, 1, 18)))).willReturn(
                Optional.of(QualityAnalysisResponse.builder()
                        .agentId(1L).consultCount(15).totalScore(3.5).build()));

        mockMvc.perform(get("/analysis/agent/daily/quality")
                        .param("agentId", "1")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value(1))
                .andExpect(jsonPath("$.totalScore").value(3.5));
    }

    @Test
    @DisplayName("일별 응대품질 전체 목록 조회 성공")
    void getDailyQuality_all_200() throws Exception {
        mockAuth("관리자");
        given(qualityAnalysisService.getDailyAll(eq(LocalDate.of(2025, 1, 18)))).willReturn(
                List.of(
                        QualityAnalysisResponse.builder().agentId(1L).totalScore(3.5).build(),
                        QualityAnalysisResponse.builder().agentId(2L).totalScore(4.0).build()
                ));

        mockMvc.perform(get("/analysis/agent/daily/quality")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].agentId").value(1))
                .andExpect(jsonPath("$[1].agentId").value(2));
    }

    // ── 주별 ─────────────────────────────────────────────

    @Test
    @DisplayName("주별 응대품질 특정 상담사 조회 성공")
    void getWeeklyQuality_withAgent_200() throws Exception {
        mockAuth();
        given(qualityAnalysisService.getWeeklyByAgent(eq(1L), eq(LocalDate.of(2025, 1, 18)))).willReturn(
                Optional.of(QualityAnalysisResponse.builder()
                        .agentId(1L).startDate("2025-01-13").endDate("2025-01-19")
                        .totalScore(3.8).build()));

        mockMvc.perform(get("/analysis/agent/weekly/quality")
                        .param("agentId", "1")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").value(3.8));
    }

    // ── 월별 ─────────────────────────────────────────────

    @Test
    @DisplayName("월별 응대품질 특정 상담사 조회 성공")
    void getMonthlyQuality_withAgent_200() throws Exception {
        mockAuth();
        given(qualityAnalysisService.getMonthlyByAgent(eq(1L), eq(LocalDate.of(2025, 1, 15)))).willReturn(
                Optional.of(QualityAnalysisResponse.builder()
                        .agentId(1L).startDate("2025-01-01").endDate("2025-01-31")
                        .totalScore(4.1).build()));

        mockMvc.perform(get("/analysis/agent/monthly/quality")
                        .param("agentId", "1")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").value(4.1));
    }
}
