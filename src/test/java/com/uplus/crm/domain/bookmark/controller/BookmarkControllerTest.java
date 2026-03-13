package com.uplus.crm.domain.bookmark.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.bookmark.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(BookmarkController.class)
@Import(SecurityConfig.class)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookmarkService bookmarkService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private static final String TOKEN = "Bearer test-token";

    @BeforeEach
    void setUp() {
        // JWT 인증 우회 설정
        given(jwtUtil.isValid("test-token")).willReturn(true);
        given(jwtUtil.getEmpId("test-token")).willReturn(1);
    }

    @Test
    @DisplayName("POST /bookmarks/manuals/{manualId} - 매뉴얼 북마크 추가 성공")
    void addManualBookmark_Success() throws Exception {
        mockMvc.perform(post("/bookmarks/manuals/1")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookmarks/manuals - 매뉴얼 북마크 목록 조회 성공")
    void getManualBookmarks_Success() throws Exception {
        given(bookmarkService.getManualBookmarks(anyInt())).willReturn(List.of());

        mockMvc.perform(get("/bookmarks/manuals")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /bookmarks/consultations/{consultId} - 상담 북마크 추가 성공")
    void addConsultationBookmark_Success() throws Exception {
        mockMvc.perform(post("/bookmarks/consultations/100")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /bookmarks/consultations/{consultId}/detail - 상담 북마크 상세 조회 성공")
    void getConsultationBookmarkDetail_Success() throws Exception {
        mockMvc.perform(get("/bookmarks/consultations/100/detail")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }
}