package com.uplus.crm.domain.summary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.summary.dto.FilterGroupDetailResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupListResponse;
import com.uplus.crm.domain.summary.dto.FilterResponse;
import com.uplus.crm.domain.summary.service.FilterGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(FilterGroupController.class)
@Import(SecurityConfig.class)
class FilterGroupControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FilterGroupService filterGroupService;

    @MockitoBean
    JwtUtil jwtUtil;

    @MockitoBean
    EmployeeRepository employeeRepository; // JwtAuthFilter 의존성

    // JWT 인증 우회용 헬퍼
    private void mockJwtAuth() {
        given(jwtUtil.isValid(any())).willReturn(true);
        given(jwtUtil.getEmpId(any())).willReturn(1);
    }

    // ─────────────────────────────────────────────
    // GET /filters — 필터 정의 목록 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 정의 목록 조회 성공 — 200 OK")
    void getFilterDefinitions_success() throws Exception {
        // given
        List<FilterResponse> filters = List.of(
                FilterResponse.builder()
                        .filterId(30).filterKey("keyword").filterName("키워드").build(),
                FilterResponse.builder()
                        .filterId(34).filterKey("consult_status").filterName("처리상태").build()
        );
        given(filterGroupService.getFilterDefinitions()).willReturn(filters);

        // when & then (인증 불필요 — permitAll이 아닌 경우 mockJwtAuth 필요)
        mockJwtAuth();
        mockMvc.perform(get("/filters")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters").isArray())
                .andExpect(jsonPath("$.filters.length()").value(2))
                .andExpect(jsonPath("$.filters[0].filterKey").value("keyword"));
    }

    // ─────────────────────────────────────────────
    // POST /search-filters — 검색 조건 저장
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("검색 조건 저장 성공 — 201 Created")
    void createFilterGroup_success() throws Exception {
        // given
        mockJwtAuth();

        FilterGroupDetailResponse response = FilterGroupDetailResponse.builder()
                .filterGroupId(1)
                .groupName("자주 쓰는 필터")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .filters(List.of(
                        FilterGroupDetailResponse.FilterCustomItemResponse.builder()
                                .filterCustomId(1).filterId(30).filterKey("keyword")
                                .filterName("키워드").filterValue("해지")
                                .createdAt(LocalDateTime.now()).build()
                ))
                .build();

        given(filterGroupService.createFilterGroup(eq(1), any())).willReturn(response);

        String requestBody = """
                {
                    "groupName": "자주 쓰는 필터",
                    "sortOrder": 1,
                    "filters": [
                        { "filterId": 30, "filterValue": "해지" }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(post("/search-filters")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filterGroupId").value(1))
                .andExpect(jsonPath("$.groupName").value("자주 쓰는 필터"))
                .andExpect(jsonPath("$.filters[0].filterValue").value("해지"));
    }

    @Test
    @DisplayName("검색 조건 저장 실패 — groupName 누락 → 400")
    void createFilterGroup_fail_missingGroupName() throws Exception {
        // given
        mockJwtAuth();

        String requestBody = """
                {
                    "filters": [
                        { "filterId": 30, "filterValue": "해지" }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(post("/search-filters")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("검색 조건 저장 실패 — filters 비어있음 → 400")
    void createFilterGroup_fail_emptyFilters() throws Exception {
        // given
        mockJwtAuth();

        String requestBody = """
                {
                    "groupName": "테스트",
                    "filters": []
                }
                """;

        // when & then
        mockMvc.perform(post("/search-filters")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("검색 조건 저장 실패 — JWT 토큰 없음 → 403")
    void createFilterGroup_fail_noToken() throws Exception {
        // given — JWT 미설정

        String requestBody = """
                {
                    "groupName": "테스트",
                    "filters": [
                        { "filterId": 30, "filterValue": "해지" }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(post("/search-filters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // GET /search-filters — 내 필터 그룹 목록 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("내 필터 그룹 목록 조회 성공 — 200 OK")
    void getMyFilterGroups_success() throws Exception {
        // given
        mockJwtAuth();

        List<FilterGroupListResponse> groups = List.of(
                FilterGroupListResponse.builder()
                        .filterGroupId(1).groupName("자주 쓰는 필터")
                        .sortOrder(0).createdAt(LocalDateTime.now()).filterCount(3).build()
        );
        given(filterGroupService.getMyFilterGroups(1)).willReturn(groups);

        // when & then
        mockMvc.perform(get("/search-filters")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups[0].groupName").value("자주 쓰는 필터"))
                .andExpect(jsonPath("$.groups[0].filterCount").value(3));
    }

    // ─────────────────────────────────────────────
    // GET /search-filters/{id} — 필터 그룹 상세 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 상세 조회 성공 — 200 OK")
    void getFilterGroupDetail_success() throws Exception {
        // given
        mockJwtAuth();

        FilterGroupDetailResponse response = FilterGroupDetailResponse.builder()
                .filterGroupId(1)
                .groupName("자주 쓰는 필터")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .filters(List.of(
                        FilterGroupDetailResponse.FilterCustomItemResponse.builder()
                                .filterCustomId(1).filterId(30).filterKey("keyword")
                                .filterName("키워드").filterValue("해지")
                                .createdAt(LocalDateTime.now()).build()
                ))
                .build();

        given(filterGroupService.getFilterGroupDetail(1, 1)).willReturn(response);

        // when & then
        mockMvc.perform(get("/search-filters/1")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterGroupId").value(1))
                .andExpect(jsonPath("$.filters[0].filterKey").value("keyword"));
    }

    @Test
    @DisplayName("필터 그룹 상세 조회 실패 — 존재하지 않는 그룹 → 404")
    void getFilterGroupDetail_fail_notFound() throws Exception {
        // given
        mockJwtAuth();
        given(filterGroupService.getFilterGroupDetail(9999, 1))
                .willThrow(new BusinessException(ErrorCode.FILTER_GROUP_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/search-filters/9999")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("FILTER_GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("필터 그룹 상세 조회 실패 — 본인 소유 아님 → 403")
    void getFilterGroupDetail_fail_forbidden() throws Exception {
        // given
        mockJwtAuth();
        given(filterGroupService.getFilterGroupDetail(1, 1))
                .willThrow(new BusinessException(ErrorCode.FORBIDDEN_ACCESS));

        // when & then
        mockMvc.perform(get("/search-filters/1")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN_ACCESS"));
    }

    // ─────────────────────────────────────────────
    // PUT /search-filters/{id} — 필터 그룹 수정
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 수정 성공 — 200 OK")
    void updateFilterGroup_success() throws Exception {
        // given
        mockJwtAuth();

        FilterGroupDetailResponse response = FilterGroupDetailResponse.builder()
                .filterGroupId(1)
                .groupName("VIP 고객 필터")
                .sortOrder(3)
                .createdAt(LocalDateTime.now())
                .filters(List.of(
                        FilterGroupDetailResponse.FilterCustomItemResponse.builder()
                                .filterCustomId(7).filterId(30).filterKey("keyword")
                                .filterName("키워드").filterValue("요금제변경")
                                .createdAt(LocalDateTime.now()).build()
                ))
                .build();

        given(filterGroupService.updateFilterGroup(eq(1), eq(1), any())).willReturn(response);

        String requestBody = """
                {
                    "groupName": "VIP 고객 필터",
                    "sortOrder": 3,
                    "filters": [
                        { "filterId": 30, "filterValue": "요금제변경" }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(put("/search-filters/1")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("VIP 고객 필터"))
                .andExpect(jsonPath("$.sortOrder").value(3))
                .andExpect(jsonPath("$.filters[0].filterValue").value("요금제변경"));
    }

    // ─────────────────────────────────────────────
    // DELETE /search-filters/{id} — 필터 그룹 삭제
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 삭제 성공 — 200 OK")
    void deleteFilterGroup_success() throws Exception {
        // given
        mockJwtAuth();
        willDoNothing().given(filterGroupService).deleteFilterGroup(1, 1);

        // when & then
        mockMvc.perform(delete("/search-filters/1")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("필터 그룹이 삭제되었습니다"));
    }

    @Test
    @DisplayName("필터 그룹 삭제 실패 — 존재하지 않는 그룹 → 404")
    void deleteFilterGroup_fail_notFound() throws Exception {
        // given
        mockJwtAuth();
        willThrow(new BusinessException(ErrorCode.FILTER_GROUP_NOT_FOUND))
                .given(filterGroupService).deleteFilterGroup(9999, 1);

        // when & then
        mockMvc.perform(delete("/search-filters/9999")
                        .header("Authorization", "Bearer mock-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("FILTER_GROUP_NOT_FOUND"));
    }

    // ─────────────────────────────────────────────
    // PUT /search-filters/order — 정렬 순서 변경
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("정렬 순서 변경 성공 — 200 OK")
    void updateFilterGroupOrder_success() throws Exception {
        // given
        mockJwtAuth();
        willDoNothing().given(filterGroupService).updateFilterGroupOrder(eq(1), any());

        String requestBody = """
                {
                    "orders": [
                        { "filterGroupId": 1, "sortOrder": 2 },
                        { "filterGroupId": 2, "sortOrder": 1 }
                    ]
                }
                """;

        // when & then
        mockMvc.perform(put("/search-filters/order")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("정렬 순서가 변경되었습니다"));
    }

    @Test
    @DisplayName("정렬 순서 변경 실패 — orders 비어있음 → 400")
    void updateFilterGroupOrder_fail_emptyOrders() throws Exception {
        // given
        mockJwtAuth();

        String requestBody = """
                {
                    "orders": []
                }
                """;

        // when & then
        mockMvc.perform(put("/search-filters/order")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }
}