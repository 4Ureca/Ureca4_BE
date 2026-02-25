package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.dto.FilterGroupCreateRequest;
import com.uplus.crm.domain.summary.dto.FilterGroupCreateRequest.FilterItemRequest;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilterGroupService {

    private final FilterRepository filterRepository;
    private final FilterGroupRepository filterGroupRepository;

    // ==================== Filter (테이블 18) - 읽기 전용 ====================

    /**
     * 필터 정의 목록 조회
     * GET /api/filters
     */
    public List<FilterResponse> getFilterDefinitions() {
        return filterRepository.findAll()
                .stream()
                .map(FilterResponse::from)
                .toList();
    }

    // ==================== FilterGroup (테이블 31) - CRUD ====================

    /**
     * 검색 조건 저장 (그룹 + 커스텀 필터값 생성)
     * POST /api/search-filters
     */
    @Transactional
    public FilterGroupDetailResponse createFilterGroup(Integer empId,
            FilterGroupCreateRequest request) {

        FilterGroup filterGroup = FilterGroup.builder()
                .empId(empId)
                .groupName(request.getGroupName())
                .sortOrder(request.getSortOrder())
                .build();

        addFilterCustomsToGroup(filterGroup, request.getFilters());

        FilterGroup saved = filterGroupRepository.save(filterGroup);
        return FilterGroupDetailResponse.from(saved);
    }

    /**
     * 내 필터 그룹 목록 조회
     * GET /api/search-filters
     */
    public List<FilterGroupListResponse> getMyFilterGroups(Integer empId) {
        return filterGroupRepository
                .findAllByEmpIdOrderBySortOrderAsc(empId)
                .stream()
                .map(FilterGroupListResponse::from)
                .toList();
    }

    /**
     * 필터 그룹 상세 조회
     * GET /api/search-filters/{filterGroupId}
     */
    public FilterGroupDetailResponse getFilterGroupDetail(Integer filterGroupId, Integer empId) {
        FilterGroup filterGroup = getFilterGroupOrThrow(filterGroupId);
        validateOwnership(filterGroup, empId);
        return FilterGroupDetailResponse.from(filterGroup);
    }

    /**
     * 필터 그룹 수정 (이름 + 필터값 전체 교체)
     * PUT /api/search-filters/{filterGroupId}
     */
    @Transactional
    public FilterGroupDetailResponse updateFilterGroup(Integer filterGroupId, Integer empId,
            FilterGroupUpdateRequest request) {

        FilterGroup filterGroup = getFilterGroupOrThrow(filterGroupId);
        validateOwnership(filterGroup, empId);

        filterGroup.updateGroupName(request.getGroupName());

        // sortOrder 반영
        if (request.getSortOrder() != null) {
            filterGroup.updateSortOrder(request.getSortOrder());
        }

        filterGroup.clearFilters();
        addFilterCustomsToGroup(filterGroup, request.getFilters());

        // saveAndFlush로 DB에 즉시 반영 → ID, createdAt 생성됨
        filterGroupRepository.saveAndFlush(filterGroup);

        return FilterGroupDetailResponse.from(filterGroup);
    }

    /**
     * 필터 그룹 삭제 (hard delete)
     * DELETE /api/search-filters/{filterGroupId}
     * - filter_groups 레코드 실제 삭제
     * - filter_custom은 cascade로 함께 삭제됨
     */
    @Transactional
    public void deleteFilterGroup(Integer filterGroupId, Integer empId) {
        FilterGroup filterGroup = getFilterGroupOrThrow(filterGroupId);
        validateOwnership(filterGroup, empId);
        filterGroupRepository.delete(filterGroup);
    }

    /**
     * 필터 그룹 정렬 순서 변경
     * PUT /api/search-filters/order
     */
    @Transactional
    public void updateFilterGroupOrder(Integer empId, FilterGroupOrderRequest request) {
        List<Integer> groupIds = request.getOrders().stream()
                .map(FilterGroupOrderRequest.OrderItem::getFilterGroupId)
                .toList();

        List<FilterGroup> groups = filterGroupRepository
                .findAllByFilterGroupIdInAndEmpId(groupIds, empId);

        if (groups.size() != groupIds.size()) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS,
                    "본인 소유가 아닌 필터 그룹이 포함되어 있습니다");
        }

        request.getOrders().forEach(order -> {
            groups.stream()
                    .filter(g -> g.getFilterGroupId().equals(order.getFilterGroupId()))
                    .findFirst()
                    .ifPresent(g -> g.updateSortOrder(order.getSortOrder()));
        });
    }

    // ==================== Private 헬퍼 ====================

    private FilterGroup getFilterGroupOrThrow(Integer filterGroupId) {
        return filterGroupRepository.findById(filterGroupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILTER_GROUP_NOT_FOUND));
    }

    private void validateOwnership(FilterGroup filterGroup, Integer empId) {
        if (!filterGroup.getEmpId().equals(empId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void addFilterCustomsToGroup(FilterGroup filterGroup,
            List<FilterItemRequest> filterItems) {
        for (FilterItemRequest item : filterItems) {
            Filter filter = filterRepository.findById(item.getFilterId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.FILTER_NOT_FOUND,
                            "유효하지 않은 필터 ID입니다. filterId=" + item.getFilterId()));

            FilterCustom filterCustom = FilterCustom.builder()
                    .filter(filter)
                    .filterValue(item.getFilterValue())
                    .build();

            filterGroup.addFilterCustom(filterCustom);
        }
    }
}