package com.uplus.crm.domain.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.demo.dto.request.DemoConsultSubmitRequest;
import com.uplus.crm.domain.demo.dto.response.DemoConsultDataResponse;
import com.uplus.crm.domain.demo.dto.response.DemoConsultSubmitResponse;
import com.uplus.crm.domain.demo.service.DemoConsultationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DemoConsultationController.class)
@Import(SecurityConfig.class)
class DemoConsultationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean DemoConsultationService demoConsultationService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    // ── 인증 헬퍼 ────────────────────────────────────────────────────────────

    private void mockAgentAuth() {
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

    private void mockAdminAuth() {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(2);

        JobRole role = mock(JobRole.class);
        given(role.getRoleName()).willReturn("관리자");
        EmployeeDetail detail = mock(EmployeeDetail.class);
        given(detail.getJobRole()).willReturn(role);
        Employee employee = mock(Employee.class);
        given(employee.getEmployeeDetail()).willReturn(detail);
        given(employeeRepository.findByIdWithDetails(2)).willReturn(Optional.of(employee));
    }

    // ── 공통 응답 픽스처 ──────────────────────────────────────────────────────

    private DemoConsultDataResponse stubDataResponse() {
        return new DemoConsultDataResponse(
                1L, "홍길동", "010-1234-5678", "개인", "남성",
                LocalDate.of(1990, 1, 1), "VIP", "hong@example.com",
                List.of(),
                "CALL", "CAT001", "요금", "청구", "과금오류",
                180, null, null, null, null
        );
    }

    // ── GET /demo/consultation ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /demo/consultation - 상담사 인증 시 200 OK + 고객정보 반환")
    void getRandomConsultData_agentAuth_200() throws Exception {
        mockAgentAuth();
        given(demoConsultationService.getRandomConsultData()).willReturn(stubDataResponse());

        mockMvc.perform(get("/demo/consultation")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.customerName").value("홍길동"))
                .andExpect(jsonPath("$.data.channel").value("CALL"))
                .andExpect(jsonPath("$.data.categoryCode").value("CAT001"))
                .andExpect(jsonPath("$.data.durationSec").value(180))
                .andExpect(jsonPath("$.data.iamIssue").doesNotExist())
                .andExpect(jsonPath("$.data.iamAction").doesNotExist())
                .andExpect(jsonPath("$.data.iamMemo").doesNotExist());
    }

    @Test
    @DisplayName("GET /demo/consultation - 관리자 인증 시도 200 OK")
    void getRandomConsultData_adminAuth_200() throws Exception {
        mockAdminAuth();
        given(demoConsultationService.getRandomConsultData()).willReturn(stubDataResponse());

        mockMvc.perform(get("/demo/consultation")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /demo/consultation - 토큰 없으면 403 Forbidden")
    void getRandomConsultData_noToken_403() throws Exception {
        mockMvc.perform(get("/demo/consultation"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /demo/consultation - 데이터 없으면 404 + CONSULTATION_NOT_FOUND")
    void getRandomConsultData_noData_404() throws Exception {
        mockAgentAuth();
        given(demoConsultationService.getRandomConsultData())
                .willThrow(new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        mockMvc.perform(get("/demo/consultation")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CONSULTATION_NOT_FOUND"));
    }

    // ── POST /demo/consultation ────────────────────────────────────────────

    @Test
    @DisplayName("POST /demo/consultation - 정상 요청 시 201 Created + consultId 반환")
    void submitConsult_agentAuth_201() throws Exception {
        mockAgentAuth();

        DemoConsultSubmitRequest request = new DemoConsultSubmitRequest(
                1L, "CALL", "CAT001", 180, null,
                "고객 요금 오류 제기", "시스템 확인", null, null
        );

        DemoConsultSubmitResponse response = new DemoConsultSubmitResponse(
                99L, LocalDateTime.of(2026, 3, 3, 10, 0, 0)
        );

        given(demoConsultationService.submitConsult(any(DemoConsultSubmitRequest.class), eq(1)))
                .willReturn(response);

        mockMvc.perform(post("/demo/consultation")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상담이 등록되었습니다."))
                .andExpect(jsonPath("$.data.consultId").value(99));
    }

    @Test
    @DisplayName("POST /demo/consultation - customerId 누락 시 400 Bad Request")
    void submitConsult_missingCustomerId_400() throws Exception {
        mockAgentAuth();

        String body = """
                {
                  "channel": "CALL",
                  "categoryCode": "CAT001",
                  "durationSec": 180
                }
                """;

        mockMvc.perform(post("/demo/consultation")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /demo/consultation - channel 누락 시 400 Bad Request")
    void submitConsult_missingChannel_400() throws Exception {
        mockAgentAuth();

        String body = """
                {
                  "customerId": 1,
                  "categoryCode": "CAT001",
                  "durationSec": 180
                }
                """;

        mockMvc.perform(post("/demo/consultation")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /demo/consultation - durationSec이 0 이하이면 400 Bad Request")
    void submitConsult_invalidDurationSec_400() throws Exception {
        mockAgentAuth();

        String body = """
                {
                  "customerId": 1,
                  "channel": "CALL",
                  "categoryCode": "CAT001",
                  "durationSec": 0
                }
                """;

        mockMvc.perform(post("/demo/consultation")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("POST /demo/consultation - 토큰 없으면 403 Forbidden")
    void submitConsult_noToken_403() throws Exception {
        String body = """
                {
                  "customerId": 1,
                  "channel": "CALL",
                  "categoryCode": "CAT001",
                  "durationSec": 180
                }
                """;

        mockMvc.perform(post("/demo/consultation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
