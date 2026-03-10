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
import com.uplus.crm.domain.analysis.dto.AgentReportResponse;
import com.uplus.crm.domain.analysis.service.AgentReportService;
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

@WebMvcTest(AgentReportController.class)
@Import(SecurityConfig.class)
class AgentReportControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AgentReportService agentReportService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    private static final String AUTH = "Bearer mock-token";

    private void mockAuth() {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(1);
        JobRole role = mock(JobRole.class);
        given(role.getRoleName()).willReturn("상담사");
        EmployeeDetail detail = mock(EmployeeDetail.class);
        given(detail.getJobRole()).willReturn(role);
        Employee employee = mock(Employee.class);
        given(employee.getEmployeeDetail()).willReturn(detail);
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.of(employee));
    }

    @Test
    @DisplayName("일별 상담사 리포트 조회 성공")
    void getDailyReport_200() throws Exception {
        mockAuth();
        given(agentReportService.getDailyReport(eq(1L), eq(LocalDate.of(2025, 1, 18)))).willReturn(
                Optional.of(AgentReportResponse.builder()
                        .agentId(1L).consultCount(15).avgDurationMinutes(8.5)
                        .customerSatisfaction(4.2).categoryRanking(List.of()).build()));

        mockMvc.perform(get("/analysis/agent/daily")
                        .param("agentId", "1")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value(1))
                .andExpect(jsonPath("$.consultCount").value(15));
    }

    @Test
    @DisplayName("주별 상담사 리포트 조회 성공")
    void getWeeklyReport_200() throws Exception {
        mockAuth();
        given(agentReportService.getWeeklyReport(eq(1L), eq(LocalDate.of(2025, 1, 18)))).willReturn(
                Optional.of(AgentReportResponse.builder()
                        .agentId(1L).startDate("2025-01-13").endDate("2025-01-19")
                        .consultCount(50).build()));

        mockMvc.perform(get("/analysis/agent/weekly")
                        .param("agentId", "1")
                        .param("date", "2025-01-18")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultCount").value(50));
    }

    @Test
    @DisplayName("월별 상담사 리포트 조회 성공")
    void getMonthlyReport_200() throws Exception {
        mockAuth();
        given(agentReportService.getMonthlyReport(eq(1L), eq(LocalDate.of(2025, 1, 15)))).willReturn(
                Optional.of(AgentReportResponse.builder()
                        .agentId(1L).startDate("2025-01-01").endDate("2025-01-31")
                        .consultCount(200).customerSatisfaction(4.0).build()));

        mockMvc.perform(get("/analysis/agent/monthly")
                        .param("agentId", "1")
                        .param("date", "2025-01-15")
                        .header("Authorization", AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultCount").value(200));
    }
}
