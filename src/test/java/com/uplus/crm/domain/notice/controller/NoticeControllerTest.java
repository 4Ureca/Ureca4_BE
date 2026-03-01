package com.uplus.crm.domain.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.notice.dto.response.NoticeListResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.service.NoticeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoticeController.class)
@Import(SecurityConfig.class)
class NoticeControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  NoticeService noticeService;

  @MockitoBean
  JwtUtil jwtUtil;

  private void mockJwtAuth() {
    given(jwtUtil.isValid(any())).willReturn(true);
    given(jwtUtil.getEmpId(any())).willReturn(1);
  }

  @Test
  @DisplayName("공지 생성 성공 - 201 Created")
  void createNotice_success() throws Exception {
    mockJwtAuth();

    NoticeResponse response = NoticeResponse.builder()
        .noticeId(1)
        .title("공지 제목")
        .content("공지 내용")
        .empId(1)
        .isPinned(true)
        .viewCount(0)
        .status(NoticeStatus.ACTIVE)
        .createdAt(LocalDateTime.of(2026, 2, 22, 10, 0))
        .visibleFrom(LocalDateTime.of(2026, 2, 22, 10, 0))
        .visibleTo(LocalDateTime.of(2026, 2, 23, 10, 0))
        .build();

    given(noticeService.createNotice(eq(1), any())).willReturn(response);

    String body = """
                {
                  "title": "공지 제목",
                  "content": "공지 내용",
                  "status": "ACTIVE",
                  "isPinned": true,
                  "visibleFrom": "2026-02-22T10:00:00",
                  "visibleTo": "2026-02-23T10:00:00"
                }
                """;

    mockMvc.perform(post("/notice")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.noticeId").value(1))
        .andExpect(jsonPath("$.title").value("공지 제목"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("공지 생성 실패 - title 누락으로 400")
  void createNotice_fail_validation() throws Exception {
    mockJwtAuth();

    String body = """
                {
                  "content": "공지 내용",
                  "status": "ACTIVE"
                }
                """;

    mockMvc.perform(post("/notice")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.fieldErrors.title").exists());
  }

  @Test
  @DisplayName("공지 목록 조회 성공 - 페이징 응답")
  void getNotices_success() throws Exception {
    mockJwtAuth();

    NoticeListResponse response = NoticeListResponse.builder()
        .content(List.of(
            NoticeResponse.builder()
                .noticeId(1)
                .title("첫 공지")
                .content("내용")
                .empId(1)
                .isPinned(true)
                .viewCount(11)
                .status(NoticeStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 2, 22, 10, 0))
                .build()
        ))
        .totalElements(21)
        .totalPages(3)
        .page(0)
        .size(10)
        .build();

    given(noticeService.getNotices(0, 10)).willReturn(response);

    mockMvc.perform(get("/notice")
            .header("Authorization", "Bearer mock-token")
            .param("page", "0")
            .param("size", "10"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("첫 공지"))
        .andExpect(jsonPath("$.totalElements").value(21))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @DisplayName("공지 목록 조회 실패 - 잘못된 페이징 파라미터로 400")
  void getNotices_fail_invalidPaging() throws Exception {
    mockJwtAuth();

    given(noticeService.getNotices(-1, 0))
        .willThrow(new BusinessException(ErrorCode.INVALID_INPUT,
            "page는 0 이상, size는 1 이상이어야 합니다."));

    mockMvc.perform(get("/notice")
            .header("Authorization", "Bearer mock-token")
            .param("page", "-1")
            .param("size", "0"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
  }

  @Test
  @DisplayName("공지 상세 조회 실패 - 없는 공지면 404")
  void getNotice_fail_notFound() throws Exception {
    mockJwtAuth();

    given(noticeService.getNotice(999))
        .willThrow(new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

    mockMvc.perform(get("/notice/999")
            .header("Authorization", "Bearer mock-token"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOTICE_NOT_FOUND"));
  }

  @Test
  @DisplayName("공지 수정 실패 - isPinned 누락으로 400")
  void updateNotice_fail_validation() throws Exception {
    mockJwtAuth();

    String body = """
                {
                  "title": "수정제목",
                  "content": "수정내용",
                  "status": "ACTIVE"
                }
                """;

    mockMvc.perform(put("/notice/1")
            .header("Authorization", "Bearer mock-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.fieldErrors.isPinned").exists());
  }

  @Test
  @DisplayName("공지 삭제 성공 - 200 OK")
  void deleteNotice_success() throws Exception {
    mockJwtAuth();

    mockMvc.perform(delete("/notice/1")
            .header("Authorization", "Bearer mock-token"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."));
  }

  @Test
  @DisplayName("공지 목록 조회 실패 - 토큰 없으면 403")
  void getNotices_fail_noToken() throws Exception {
    mockMvc.perform(get("/notice"))
        .andDo(print())
        .andExpect(status().isForbidden());
  }
}
