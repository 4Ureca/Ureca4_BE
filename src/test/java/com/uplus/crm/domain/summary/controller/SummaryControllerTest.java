package com.uplus.crm.domain.summary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.service.SummaryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SummaryController.class)
@Import(SecurityConfig.class)
class SummaryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    SummaryService summaryService;

    // SecurityConfig → JwtAuthFilter 의존성
    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    EmployeeRepository employeeRepository;

    // ── JWT 인증 우회 ──────────────────────────────────────────────────────

    private static final String TOKEN = "Bearer test-token";

    @BeforeEach
    void mockAuth() {
        given(jwtUtil.isValid("test-token")).willReturn(true);
        given(jwtUtil.getEmpId("test-token")).willReturn(1);
        // findByIdWithDetails 미설정 → Optional.empty() → fallback ROLE_AGENT
    }

    // ── GET /summaries ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /summaries 200 — 빈 파라미터로 전체 목록 조회")
    void list_noParams_returns200() throws Exception {
        ConsultationSummaryListResponse item = ConsultationSummaryListResponse.builder()
                .consultId(1L)
                .consultedAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .channel("CALL")
                .agentName("홍길동")
                .customerName("김고객")
                .summaryStatus("COMPLETED")
                .iamMatchRate(0.92)
                .defenseAttempted(false)
                .build();

        Page<ConsultationSummaryListResponse> page =
                new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

        given(summaryService.search(any(), any())).willReturn(page);

        mockMvc.perform(get("/summaries").header("Authorization", TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].consultId").value(1))
                .andExpect(jsonPath("$.content[0].agentName").value("홍길동"))
                .andExpect(jsonPath("$.content[0].summaryStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].iamMatchRate").value(0.92));
    }

    @Test
    @DisplayName("GET /summaries 200 — 검색 파라미터 전달")
    void list_withParams_passes200() throws Exception {
        given(summaryService.search(any(), any()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/summaries")
                        .header("Authorization", TOKEN)
                        .param("keyword", "해지")
                        .param("channel", "CALL")
                        .param("satisfactionScore", "4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /summaries 4xx — 인증 없이 접근 시 401 또는 403")
    void list_noAuth_returns4xx() throws Exception {
        // Spring Security 버전에 따라 401(Unauthorized) 또는 403(Forbidden) 반환
        mockMvc.perform(get("/summaries"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    // ── GET /summaries/{consultId} ────────────────────────────────────────

    @Test
    @DisplayName("GET /summaries/{consultId} 200 — 정상 조회")
    void detail_validId_returns200() throws Exception {
        ConsultationSummaryDetailResponse.ContentInfo content =
                ConsultationSummaryDetailResponse.ContentInfo.builder()
                        .status("COMPLETED")
                        .aiSummary("AI 요약 내용")
                        .rawTextJson("{\"script\":\"안녕하세요\"}")
                        .build();

        ConsultationSummaryDetailResponse.AnalysisInfo analysis =
                ConsultationSummaryDetailResponse.AnalysisInfo.builder()
                        .iamMatchRate(0.92)
                        .riskFlags(List.of("해지위험"))
                        .defenseAttempted(true)
                        .build();

        ConsultationSummaryDetailResponse detail =
                ConsultationSummaryDetailResponse.builder()
                        .consultId(1L)
                        .channel("CALL")
                        .durationSec(300)
                        .content(content)
                        .analysis(analysis)
                        .activeSubscriptions(List.of())
                        .build();

        given(summaryService.getDetail(1L)).willReturn(detail);

        mockMvc.perform(get("/summaries/1").header("Authorization", TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultId").value(1))
                .andExpect(jsonPath("$.channel").value("CALL"))
                .andExpect(jsonPath("$.content.aiSummary").value("AI 요약 내용"))
                .andExpect(jsonPath("$.content.rawTextJson").value("{\"script\":\"안녕하세요\"}"))
                .andExpect(jsonPath("$.analysis.iamMatchRate").value(0.92))
                .andExpect(jsonPath("$.analysis.riskFlags[0]").value("해지위험"))
                .andExpect(jsonPath("$.analysis.defenseAttempted").value(true))
                .andExpect(jsonPath("$.activeSubscriptions").isArray());
    }

    @Test
    @DisplayName("GET /summaries/{consultId} 404 — 존재하지 않는 consultId")
    void detail_notFound_returns404() throws Exception {
        given(summaryService.getDetail(999L))
                .willThrow(new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND));

        mockMvc.perform(get("/summaries/999").header("Authorization", TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /summaries/{consultId} 200 — MongoDB 없어도 부분 응답")
    void detail_partialResponse_noMongoDB_returns200() throws Exception {
        ConsultationSummaryDetailResponse partial =
                ConsultationSummaryDetailResponse.builder()
                        .consultId(2L)
                        .channel("CHATTING")
                        .durationSec(150)
                        .content(ConsultationSummaryDetailResponse.ContentInfo.builder()
                                .aiSummary(null)   // MongoDB 없음
                                .rawTextJson(null)
                                .build())
                        .analysis(ConsultationSummaryDetailResponse.AnalysisInfo.builder()
                                .iamIssue("요금 문의")
                                .build())
                        .activeSubscriptions(List.of())
                        .build();

        given(summaryService.getDetail(2L)).willReturn(partial);

        mockMvc.perform(get("/summaries/2").header("Authorization", TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultId").value(2))
                .andExpect(jsonPath("$.content.aiSummary").isEmpty())
                .andExpect(jsonPath("$.analysis.iamIssue").value("요금 문의"));
    }
}
