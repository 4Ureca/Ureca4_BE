package com.uplus.crm.domain.summary.dto;

import com.uplus.crm.domain.summary.entity.Filter;
import lombok.Builder;
import lombok.Getter;

/**
 * 필터 정의 응답 DTO (테이블 18)
 * GET /api/filters
 */
@Getter
@Builder
public class FilterResponse {

    private Integer filterId;
    private String filterKey;
    private String altCode;
    private String filterName;

    public static FilterResponse from(Filter filter) {
        return FilterResponse.builder()
                .filterId(filter.getFilterId())
                .filterKey(filter.getFilterKey())
                .altCode(filter.getAltCode())
                .filterName(filter.getFilterName())
                .build();
    }
}