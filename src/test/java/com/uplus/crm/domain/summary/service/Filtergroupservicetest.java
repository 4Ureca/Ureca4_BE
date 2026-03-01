package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.dto.FilterGroupCreateRequest;
import com.uplus.crm.domain.summary.dto.FilterGroupDetailResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupListResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupOrderRequest;
import com.uplus.crm.domain.summary.dto.FilterGroupUpdateRequest;
import com.uplus.crm.domain.summary.dto.FilterResponse;
import com.uplus.crm.domain.summary.entity.Filter;
import com.uplus.crm.domain.summary.entity.FilterCustom;
import com.uplus.crm.domain.summary.entity.FilterGroup;
import com.uplus.crm.domain.summary.repository.FilterGroupRepository;
import com.uplus.crm.domain.summary.repository.FilterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FilterGroupServiceTest {

    @InjectMocks
    private FilterGroupService filterGroupService;

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private FilterGroupRepository filterGroupRepository;

    private Filter mockFilterKeyword;
    private Filter mockFilterStatus;
    private FilterGroup mockFilterGroup;

    @BeforeEach
    void setUp() {
        // Filter 엔티티 (읽기 전용 — Reflection으로 ID 설정)
        mockFilterKeyword = createFilter(30, "keyword", "키워드");
        mockFilterStatus = createFilter(34, "consult_status", "처리상태");

        // FilterGroup 엔티티
        mockFilterGroup = FilterGroup.builder()
                .empId(1)
                .groupName("자주 쓰는 필터")
                .sortOrder(1)
                .build();
        ReflectionTestUtils.setField(mockFilterGroup, "filterGroupId", 1);
        ReflectionTestUtils.setField(mockFilterGroup, "createdAt", LocalDateTime.now());

        // FilterCustom 추가
        FilterCustom custom1 = FilterCustom.builder()
                .filter(mockFilterKeyword)
                .filterValue("해지")
                .build();
        ReflectionTestUtils.setField(custom1, "filterCustomId", 1);
        ReflectionTestUtils.setField(custom1, "createdAt", LocalDateTime.now());
        mockFilterGroup.addFilterCustom(custom1);

        FilterCustom custom2 = FilterCustom.builder()
                .filter(mockFilterStatus)
                .filterValue("COMPLETED")
                .build();
        ReflectionTestUtils.setField(custom2, "filterCustomId", 2);
        ReflectionTestUtils.setField(custom2, "createdAt", LocalDateTime.now());
        mockFilterGroup.addFilterCustom(custom2);
    }

    // ─────────────────────────────────────────────
    // GET /api/filters — 필터 정의 목록 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 정의 목록 조회 성공")
    void getFilterDefinitions_success() {
        // given
        given(filterRepository.findAll())
                .willReturn(List.of(mockFilterKeyword, mockFilterStatus));

        // when
        List<FilterResponse> result = filterGroupService.getFilterDefinitions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFilterKey()).isEqualTo("keyword");
        assertThat(result.get(1).getFilterKey()).isEqualTo("consult_status");
    }

    // ─────────────────────────────────────────────
    // POST /api/search-filters — 검색 조건 저장
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("검색 조건 저장 성공")
    void createFilterGroup_success() {
        // given
        FilterGroupCreateRequest request = createRequest(
                "자주 쓰는 필터", 1,
                List.of(
                        createFilterItem(30, "해지"),
                        createFilterItem(34, "COMPLETED")
                )
        );

        given(filterRepository.findById(30)).willReturn(Optional.of(mockFilterKeyword));
        given(filterRepository.findById(34)).willReturn(Optional.of(mockFilterStatus));
        given(filterGroupRepository.save(any(FilterGroup.class))).willReturn(mockFilterGroup);

        // when
        FilterGroupDetailResponse response = filterGroupService.createFilterGroup(1, request);

        // then
        assertThat(response.getGroupName()).isEqualTo("자주 쓰는 필터");
        assertThat(response.getFilters()).hasSize(2);
        then(filterGroupRepository).should().save(any(FilterGroup.class));
    }

    @Test
    @DisplayName("검색 조건 저장 실패 — 유효하지 않은 filterId")
    void createFilterGroup_fail_invalidFilterId() {
        // given
        FilterGroupCreateRequest request = createRequest(
                "테스트 필터", null,
                List.of(createFilterItem(999, "없는필터"))
        );

        given(filterRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> filterGroupService.createFilterGroup(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FILTER_NOT_FOUND);
                });
    }

    // ─────────────────────────────────────────────
    // GET /api/search-filters — 내 필터 그룹 목록 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("내 필터 그룹 목록 조회 성공")
    void getMyFilterGroups_success() {
        // given
        given(filterGroupRepository.findAllByEmpIdOrderBySortOrderAsc(1))
                .willReturn(List.of(mockFilterGroup));

        // when
        List<FilterGroupListResponse> result = filterGroupService.getMyFilterGroups(1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupName()).isEqualTo("자주 쓰는 필터");
        assertThat(result.get(0).getFilterCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("내 필터 그룹 목록 조회 — 데이터 없으면 빈 리스트")
    void getMyFilterGroups_empty() {
        // given
        given(filterGroupRepository.findAllByEmpIdOrderBySortOrderAsc(1))
                .willReturn(List.of());

        // when
        List<FilterGroupListResponse> result = filterGroupService.getMyFilterGroups(1);

        // then
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // GET /api/search-filters/{id} — 필터 그룹 상세 조회
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 상세 조회 성공")
    void getFilterGroupDetail_success() {
        // given
        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));

        // when
        FilterGroupDetailResponse response = filterGroupService.getFilterGroupDetail(1, 1);

        // then
        assertThat(response.getFilterGroupId()).isEqualTo(1);
        assertThat(response.getGroupName()).isEqualTo("자주 쓰는 필터");
        assertThat(response.getFilters()).hasSize(2);
        assertThat(response.getFilters().get(0).getFilterKey()).isEqualTo("keyword");
    }

    @Test
    @DisplayName("필터 그룹 상세 조회 실패 — 존재하지 않는 그룹")
    void getFilterGroupDetail_fail_notFound() {
        // given
        given(filterGroupRepository.findById(9999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> filterGroupService.getFilterGroupDetail(9999, 1))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FILTER_GROUP_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("필터 그룹 상세 조회 실패 — 본인 소유가 아닌 그룹")
    void getFilterGroupDetail_fail_notOwner() {
        // given
        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));

        // when & then (empId=999로 접근)
        assertThatThrownBy(() -> filterGroupService.getFilterGroupDetail(1, 999))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
                });
    }

    // ─────────────────────────────────────────────
    // PUT /api/search-filters/{id} — 필터 그룹 수정
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 수정 성공 — 이름, sortOrder, 필터값 전체 교체")
    void updateFilterGroup_success() {
        // given
        FilterGroupUpdateRequest request = createUpdateRequest(
                "VIP 고객 필터", 5,
                List.of(createFilterItem(30, "요금제변경"))
        );

        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));
        given(filterRepository.findById(30)).willReturn(Optional.of(mockFilterKeyword));
        given(filterGroupRepository.saveAndFlush(any(FilterGroup.class))).willReturn(mockFilterGroup);

        // when
        FilterGroupDetailResponse response = filterGroupService.updateFilterGroup(1, 1, request);

        // then
        assertThat(response).isNotNull();
        then(filterGroupRepository).should().saveAndFlush(any(FilterGroup.class));
    }

    @Test
    @DisplayName("필터 그룹 수정 실패 — 존재하지 않는 그룹")
    void updateFilterGroup_fail_notFound() {
        // given
        FilterGroupUpdateRequest request = createUpdateRequest(
                "테스트", null, List.of(createFilterItem(30, "값"))
        );

        given(filterGroupRepository.findById(9999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> filterGroupService.updateFilterGroup(9999, 1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FILTER_GROUP_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("필터 그룹 수정 실패 — 본인 소유가 아닌 그룹")
    void updateFilterGroup_fail_notOwner() {
        // given
        FilterGroupUpdateRequest request = createUpdateRequest(
                "테스트", null, List.of(createFilterItem(30, "값"))
        );

        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));

        // when & then
        assertThatThrownBy(() -> filterGroupService.updateFilterGroup(1, 999, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
                });
    }

    // ─────────────────────────────────────────────
    // DELETE /api/search-filters/{id} — 필터 그룹 삭제
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("필터 그룹 삭제 성공")
    void deleteFilterGroup_success() {
        // given
        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));

        // when
        filterGroupService.deleteFilterGroup(1, 1);

        // then
        then(filterGroupRepository).should().delete(mockFilterGroup);
    }

    @Test
    @DisplayName("필터 그룹 삭제 실패 — 존재하지 않는 그룹")
    void deleteFilterGroup_fail_notFound() {
        // given
        given(filterGroupRepository.findById(9999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> filterGroupService.deleteFilterGroup(9999, 1))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FILTER_GROUP_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("필터 그룹 삭제 실패 — 본인 소유가 아닌 그룹")
    void deleteFilterGroup_fail_notOwner() {
        // given
        given(filterGroupRepository.findById(1)).willReturn(Optional.of(mockFilterGroup));

        // when & then
        assertThatThrownBy(() -> filterGroupService.deleteFilterGroup(1, 999))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
                });
    }

    // ─────────────────────────────────────────────
    // PUT /api/search-filters/order — 정렬 순서 변경
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("정렬 순서 변경 성공")
    void updateFilterGroupOrder_success() {
        // given
        FilterGroup group2 = FilterGroup.builder()
                .empId(1)
                .groupName("두 번째 필터")
                .sortOrder(2)
                .build();
        ReflectionTestUtils.setField(group2, "filterGroupId", 2);

        FilterGroupOrderRequest request = createOrderRequest(
                List.of(
                        createOrderItem(1, 2),
                        createOrderItem(2, 1)
                )
        );

        given(filterGroupRepository.findAllByFilterGroupIdInAndEmpId(List.of(1, 2), 1))
                .willReturn(List.of(mockFilterGroup, group2));

        // when
        filterGroupService.updateFilterGroupOrder(1, request);

        // then
        assertThat(mockFilterGroup.getSortOrder()).isEqualTo(2);
        assertThat(group2.getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("정렬 순서 변경 실패 — 본인 소유가 아닌 그룹 포함")
    void updateFilterGroupOrder_fail_notOwner() {
        // given
        FilterGroupOrderRequest request = createOrderRequest(
                List.of(
                        createOrderItem(1, 2),
                        createOrderItem(99, 1)
                )
        );

        // 본인 소유 1건만 반환 → 요청 2건과 불일치
        given(filterGroupRepository.findAllByFilterGroupIdInAndEmpId(List.of(1, 99), 1))
                .willReturn(List.of(mockFilterGroup));

        // when & then
        assertThatThrownBy(() -> filterGroupService.updateFilterGroupOrder(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
                });
    }

    // ─────────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────────

    private Filter createFilter(Integer id, String key, String name) {
        Filter filter;
        try {
            var constructor = Filter.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            filter = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(filter, "filterId", id);
        ReflectionTestUtils.setField(filter, "filterKey", key);
        ReflectionTestUtils.setField(filter, "filterName", name);
        return filter;
    }

    private FilterGroupCreateRequest createRequest(String groupName, Integer sortOrder,
            List<FilterGroupCreateRequest.FilterItemRequest> filters) {
        FilterGroupCreateRequest request = new FilterGroupCreateRequest();
        ReflectionTestUtils.setField(request, "groupName", groupName);
        ReflectionTestUtils.setField(request, "sortOrder", sortOrder);
        ReflectionTestUtils.setField(request, "filters", filters);
        return request;
    }

    private FilterGroupUpdateRequest createUpdateRequest(String groupName, Integer sortOrder,
            List<FilterGroupCreateRequest.FilterItemRequest> filters) {
        FilterGroupUpdateRequest request = new FilterGroupUpdateRequest();
        ReflectionTestUtils.setField(request, "groupName", groupName);
        ReflectionTestUtils.setField(request, "sortOrder", sortOrder);
        ReflectionTestUtils.setField(request, "filters", filters);
        return request;
    }

    private FilterGroupCreateRequest.FilterItemRequest createFilterItem(Integer filterId, String filterValue) {
        FilterGroupCreateRequest.FilterItemRequest item = new FilterGroupCreateRequest.FilterItemRequest();
        ReflectionTestUtils.setField(item, "filterId", filterId);
        ReflectionTestUtils.setField(item, "filterValue", filterValue);
        return item;
    }

    private FilterGroupOrderRequest createOrderRequest(List<FilterGroupOrderRequest.OrderItem> orders) {
        FilterGroupOrderRequest request = new FilterGroupOrderRequest();
        ReflectionTestUtils.setField(request, "orders", orders);
        return request;
    }

    private FilterGroupOrderRequest.OrderItem createOrderItem(Integer filterGroupId, Integer sortOrder) {
        FilterGroupOrderRequest.OrderItem item = new FilterGroupOrderRequest.OrderItem();
        ReflectionTestUtils.setField(item, "filterGroupId", filterGroupId);
        ReflectionTestUtils.setField(item, "sortOrder", sortOrder);
        return item;
    }
}