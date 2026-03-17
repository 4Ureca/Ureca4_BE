package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.analysis.dto.agent.CategoryRankingDto;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.analysis.controller.agent.AgentReportController;
import com.uplus.crm.domain.analysis.dto.agent.AgentMetricsResponse;
import com.uplus.crm.domain.analysis.service.agent.AgentReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentReportController.class)
@Import(SecurityConfig.class)
class AgentReportControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean AgentReportService agentReportService;
  @MockitoBean JwtUtil jwtUtil;
  @MockitoBean EmployeeRepository employeeRepository;

  private static final String AUTH = "Bearer mock-token";

  private void mockAuth(String roleName) {
    given(jwtUtil.isValid(any())).willReturn(true);
    given(jwtUtil.getEmpId(any())).willReturn(1); // 테스트용 empId를 1로 고정

    JobRole role = mock(JobRole.class);
    given(role.getRoleName()).willReturn(roleName);

    EmployeeDetail detail = mock(EmployeeDetail.class);
    given(detail.getJobRole()).willReturn(role);

    Employee employee = mock(Employee.class);
    given(employee.getEmployeeDetail()).willReturn(detail);
    given(employee.getEmpId()).willReturn(1);

    given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.of(employee));
  }

  private void mockAuth() {
    mockAuth("상담사");
  }

  @Test
  @DisplayName("상담사 성과 조회 성공")
  void getMetrics_200() throws Exception {
    // given
    mockAuth(); // "상담사" 권한으로 세팅
    given(agentReportService.getMetrics(anyString(), eq(1), any(LocalDate.class)))
        .willReturn(AgentMetricsResponse.builder()
            .empId("1")
            .myConsultCount(10)
            .build());

    // when & then
    mockMvc.perform(get("/api/analysis/agent/daily/metrics")
            .header("Authorization", AUTH) // 헤더에 토큰 추가
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.empId").value("1"))
        .andExpect(jsonPath("$.myConsultCount").value(10));
  }

  @Test
  @DisplayName("관리자가 타겟 ID 없이 조회 시 400 에러")
  void getMetrics_Admin_MissingId_400() throws Exception {
    // given
    mockAuth("관리자"); // "관리자" 권한으로 세팅

    // when & then
    mockMvc.perform(get("/api/analysis/agent/daily/metrics")
            .header("Authorization", AUTH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()); // BusinessException 발생
  }

  @Test
  @DisplayName("카테고리 순위 조회 성공")
  void getCategories_200() throws Exception {
    // given
    mockAuth();
    given(agentReportService.getCategories(anyString(), eq(1), any(LocalDate.class)))
        .willReturn(List.of(CategoryRankingDto.builder().name("해지").totalCount(5).build()));

    // when & then
    mockMvc.perform(get("/api/analysis/agent/daily/categories")
            .header("Authorization", AUTH)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("해지"));
  }
}