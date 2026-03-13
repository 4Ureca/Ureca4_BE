package com.uplus.crm.domain.consultation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.anyInt;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.consultation.dto.response.ConsultDataResponse;
import com.uplus.crm.domain.consultation.service.ConsultationDetailService;
import com.uplus.crm.domain.consultation.service.ConsultationListService;
import com.uplus.crm.domain.consultation.service.ConsultationService;

@WebMvcTest({
    ConsultationController.class,
    ConsultationDetailController.class,
    ConsultationListController.class
})
@Import(SecurityConfig.class)
class ConsultationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ConsultationService consultationService;
    @MockitoBean ConsultationDetailService consultationDetailService;
    @MockitoBean ConsultationListService consultationListService;
    
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

    // ── 공통 응답 픽스처 ──────────────────────────────────────────────────────

    private ConsultDataResponse stubDataResponse() {
        return new ConsultDataResponse(
                10L, 1L, "홍길동", "010-1234-5678", "개인", "남성",
                LocalDate.of(1990, 1, 1), "VIP", "hong@example.com",
                List.of(),
                "CALL", "CAT001", "요금", "청구", "과금오류",
                180, "고객이 요금 오류 제기", "시스템 확인 후 재청구", "추후 모니터링 필요",
                null
        );
    }

    // ── [ConsultationController] GET /consultation ──────────────────────────

    @Test
    @DisplayName("GET /consultation - 인증 시 200 OK + IAM 포함 전체 상담 데이터 반환")
    void getRandomConsultData_agentAuth_200() throws Exception {
        mockAgentAuth();
        given(consultationService.getRandomConsultData()).willReturn(stubDataResponse());

        mockMvc.perform(get("/consultation")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.consultId").value(10))
                .andExpect(jsonPath("$.data.customerName").value("홍길동"));
    }

    @Test
    @DisplayName("GET /consultation - 토큰 없으면 403 Forbidden")
    void getRandomConsultData_noToken_403() throws Exception {
        mockMvc.perform(get("/consultation"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /consultation - 데이터 없으면 404 + CONSULTATION_NOT_FOUND")
    void getRandomConsultData_noData_404() throws Exception {
        mockAgentAuth();
        given(consultationService.getRandomConsultData())
                .willThrow(new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        mockMvc.perform(get("/consultation")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CONSULTATION_NOT_FOUND"));
    }

    // ── [ConsultationDetailController] GET /consultation/detail ──────────────

    @Test
    @DisplayName("GET /consultation/detail - 상담 상세 정보 조회 성공 (200)")
    void getConsultationDetail_200() throws Exception {
        mockAgentAuth();
        // 서비스 응답 Mocking (상세 정보 DTO가 null이 아니라고 가정하거나 null로 진행)
        given(consultationDetailService.getConsultationDetail(12L)).willReturn(null);

        mockMvc.perform(get("/consultation/detail")
                        .param("consultId", "12")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ── [ConsultationListController] GET /consultation/list ────────────────

    @Test
    @DisplayName("GET /consultation/list - 목록 필터 조회 성공 (200)")
    void getConsultationList_200() throws Exception {
        mockAgentAuth();
        // 서비스 응답 Mocking
        given(consultationListService.getConsultationList(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(null);

        mockMvc.perform(get("/consultation/list")
                        .param("keyword", "인터넷")
                        .param("channel", "CALL")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}