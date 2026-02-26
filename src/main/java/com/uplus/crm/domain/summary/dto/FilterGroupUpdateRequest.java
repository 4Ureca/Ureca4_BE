package com.uplus.crm.domain.summary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 필터 그룹 수정 요청 DTO
 * PUT /api/search-filters/{filterGroupId}
 */
@Getter
@NoArgsConstructor
public class FilterGroupUpdateRequest {

    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 100, message = "그룹 이름은 100자 이내여야 합니다")
    private String groupName;

    private Integer sortOrder;

    @NotEmpty(message = "필터 조건은 최소 1개 이상이어야 합니다")
    @Valid
    private List<FilterGroupCreateRequest.FilterItemRequest> filters;
}