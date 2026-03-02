package com.uplus.crm.domain.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.filter.JwtAuthFilter;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeSummary;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import com.uplus.crm.domain.notice.service.NoticeService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoticeController.class)
@Import(SecurityConfig.class)
class NoticeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean NoticeService noticeService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository;

    /**
     * JWT 유효 토큰 + 관리자 역할을 설정한다 (ROLE_ADMIN).
     */
    private void mockAdminAuth() {
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

    /**
     * JWT 유효 토큰 + 상담사 역할을 설정한다 (ROLE_AGENT).
     */
    private void mockAgentAuth() {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(2);

        JobRole role = mock(JobRole.class);
        given(role.getRoleName()).willReturn("상담사");
        EmployeeDetail detail = mock(EmployeeDetail.class);
        given(detail.getJobRole()).willReturn(role);
        Employee employee = mock(Employee.class);
        given(employee.getEmployeeDetail()).willReturn(detail);
        given(employeeRepository.findByIdWithDetails(2)).willReturn(Optional.of(employee));
    }

    // ── 공지 등록 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지 등록 성공 (ADMIN) - 201 Created")
    void createNotice_adminSuccess() throws Exception {
        mockAdminAuth();

        NoticeResponse response = new NoticeResponse(
                1, "공지 제목", "공지 내용", "관리자",
                NoticeType.GENERAL, TargetRole.ALL,
                NoticeStatus.ACTIVE, false, 0,
                LocalDateTime.of(2026, 3, 2, 10, 0), null, null, true);

        given(noticeService.createNotice(any(), eq(1))).willReturn(response);

        String body = """
                {
                  "title": "공지 제목",
                  "content": "공지 내용",
                  "sendNotification": false
                }
                """;

        mockMvc.perform(post("/v1/notices")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("공지 제목"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("공지 등록 실패 - AGENT는 403 Forbidden")
    void createNotice_agentForbidden() throws Exception {
        mockAgentAuth();

        String body = """
                {
                  "title": "공지 제목",
                  "content": "공지 내용",
                  "sendNotification": false
                }
                """;

        mockMvc.perform(post("/v1/notices")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("공지 등록 실패 - title 누락이면 400")
    void createNotice_titleMissing_400() throws Exception {
        mockAdminAuth();

        String body = """
                { "content": "공지 내용", "sendNotification": false }
                """;

        mockMvc.perform(post("/v1/notices")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ── 공지 목록 조회 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지 목록 조회 - ADMIN은 전체 목록 반환")
    void getNoticeList_adminSeesAll() throws Exception {
        mockAdminAuth();

        NoticeSummary summary = new NoticeSummary(
                1, "공지 제목", NoticeType.GENERAL, NoticeStatus.ACTIVE,
                false, true, "관리자", 10, LocalDateTime.now());

        given(noticeService.getNoticeList(eq(1), eq("관리자"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/v1/notices")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("공지 제목"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("공지 목록 조회 - AGENT는 ACTIVE 목록만 반환")
    void getNoticeList_agentSeesActiveOnly() throws Exception {
        mockAgentAuth();

        NoticeSummary summary = new NoticeSummary(
                2, "상담사 공지", NoticeType.GENERAL, NoticeStatus.ACTIVE,
                false, false, "관리자", 5, LocalDateTime.now());

        given(noticeService.getNoticeList(eq(2), eq("상담사"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/v1/notices")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("상담사 공지"));
    }

    // ── 공지 상세 조회 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지 상세 조회 실패 - 없는 공지면 404")
    void getNoticeDetail_notFound() throws Exception {
        mockAdminAuth();

        given(noticeService.getNoticeDetail(eq(999), eq(1)))
                .willThrow(new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        mockMvc.perform(get("/v1/notices/999")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOTICE_NOT_FOUND"));
    }

    // ── 공지 삭제 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지 삭제 성공 (ADMIN) - 200 OK")
    void deleteNotice_adminSuccess() throws Exception {
        mockAdminAuth();

        mockMvc.perform(delete("/v1/notices/1")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."));
    }

    @Test
    @DisplayName("토큰 없으면 403 Forbidden")
    void noToken_403() throws Exception {
        mockMvc.perform(get("/v1/notices"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
