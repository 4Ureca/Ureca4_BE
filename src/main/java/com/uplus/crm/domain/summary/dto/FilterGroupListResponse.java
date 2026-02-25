package com.uplus.crm.domain.summary.dto;

import com.uplus.crm.domain.summary.entity.FilterGroup;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 필터 그룹 목록 조회용 응답 DTO
 * GET /api/search-filters
 */
@Getter
@Builder
public class FilterGroupListResponse {

    private Integer filterGroupId;
    private String groupName;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private Integer filterCount;

    public static FilterGroupListResponse from(FilterGroup filterGroup) {
        return FilterGroupListResponse.builder()
                .filterGroupId(filterGroup.getFilterGroupId())
                .groupName(filterGroup.getGroupName())
                .sortOrder(filterGroup.getSortOrder())
                .createdAt(filterGroup.getCreatedAt())
                .filterCount(filterGroup.getFilterCustoms().size())
                .build();
    }
}