package com.uplus.crm.domain.summary.dto;

import com.uplus.crm.domain.summary.entity.FilterCustom;
import com.uplus.crm.domain.summary.entity.FilterGroup;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 필터 그룹 상세 응답 DTO
 * - POST /api/search-filters (생성 응답)
 * - GET /api/search-filters/{id} (상세 조회)
 * - PUT /api/search-filters/{id} (수정 응답)
 */
@Getter
@Builder
public class FilterGroupDetailResponse {

    private Integer filterGroupId;
    private String groupName;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private List<FilterCustomItemResponse> filters;

    @Getter
    @Builder
    public static class FilterCustomItemResponse {

        private Integer filterCustomId;
        private Integer filterId;
        private String filterKey;
        private String filterName;
        private String filterValue;
        private LocalDateTime createdAt;

        public static FilterCustomItemResponse from(FilterCustom filterCustom) {
            return FilterCustomItemResponse.builder()
                    .filterCustomId(filterCustom.getFilterCustomId())
                    .filterId(filterCustom.getFilter().getFilterId())
                    .filterKey(filterCustom.getFilter().getFilterKey())
                    .filterName(filterCustom.getFilter().getFilterName())
                    .filterValue(filterCustom.getFilterValue())
                    .createdAt(filterCustom.getCreatedAt())
                    .build();
        }
    }

    public static FilterGroupDetailResponse from(FilterGroup filterGroup) {
        List<FilterCustomItemResponse> filterItems = filterGroup.getFilterCustoms()
                .stream()
                .map(FilterCustomItemResponse::from)
                .toList();

        return FilterGroupDetailResponse.builder()
                .filterGroupId(filterGroup.getFilterGroupId())
                .groupName(filterGroup.getGroupName())
                .sortOrder(filterGroup.getSortOrder())
                .createdAt(filterGroup.getCreatedAt())
                .filters(filterItems)
                .build();
    }
}