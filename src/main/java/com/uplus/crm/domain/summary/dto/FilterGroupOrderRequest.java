package com.uplus.crm.domain.summary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 필터 그룹 정렬 순서 변경 요청 DTO
 * PUT /api/search-filters/order
 */
@Getter
@NoArgsConstructor
public class FilterGroupOrderRequest {

    @NotEmpty(message = "정렬 순서 항목은 최소 1개 이상이어야 합니다")
    @Valid
    private List<OrderItem> orders;

    @Getter
    @NoArgsConstructor
    public static class OrderItem {

        @NotNull(message = "필터 그룹 ID는 필수입니다")
        private Integer filterGroupId;

        @NotNull(message = "정렬 순서는 필수입니다")
        private Integer sortOrder;
    }
}