package com.uplus.crm.domain.summary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검색 조건 저장 요청 DTO
 * POST /api/search-filters
 */
@Getter
@NoArgsConstructor
public class FilterGroupCreateRequest {

    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 100, message = "그룹 이름은 100자 이내여야 합니다")
    private String groupName;
    private Integer sortOrder;

    @NotEmpty(message = "필터 조건은 최소 1개 이상이어야 합니다")
    @Valid
    private List<FilterItemRequest> filters;

    @Getter
    @NoArgsConstructor
    public static class FilterItemRequest {

        @NotNull(message = "필터 ID는 필수입니다")
        private Integer filterId;

        @NotBlank(message = "필터 값은 필수입니다")
        private String filterValue;
    }
}