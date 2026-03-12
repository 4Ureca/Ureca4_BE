package com.uplus.crm.domain.summary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "검색 조건 저장 요청")
@Getter
@NoArgsConstructor
public class FilterGroupCreateRequest {

    @Schema(description = "필터 그룹 이름 (저장 이름)", example = "VIP 고객 상담 모니터링", maxLength = 100)
    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 100, message = "그룹 이름은 100자 이내여야 합니다")
    private String groupName;

    @Schema(description = "목록 표시 순서 (생략 시 null)", example = "1")
    private Integer sortOrder;

    @Schema(description = "필터 조건 목록 (최소 1개 이상). customer_grade(13)·risk_type(14)은 같은 filterId를 여러 개 보내면 OR 조건으로 처리됩니다.")
    @NotEmpty(message = "필터 조건은 최소 1개 이상이어야 합니다")
    @Valid
    private List<FilterItemRequest> filters;

    @Schema(description = "개별 필터 조건 항목")
    @Getter
    @NoArgsConstructor
    public static class FilterItemRequest {

        @Schema(
            description = """
                필터 ID. GET /api/filters 로 전체 목록 조회 가능.
                | ID | filterKey            | 설명                    | filterValue 형식 |
                |----|----------------------|-------------------------|-----------------|
                | 1  | keyword              | 자율검색 (상담내용/상품명 OR) | 자유 텍스트 |
                | 2  | consult_from         | 상담 시작일              | yyyy-MM-dd |
                | 3  | consult_to           | 상담 종료일              | yyyy-MM-dd |
                | 4  | consultant_name      | 담당 상담사 이름          | 자유 텍스트 (부분 일치) |
                | 5  | category_name        | 상담 카테고리명           | 카테고리명 문자열 |
                | 6  | channel              | 상담 채널                | CALL 또는 CHATTING |
                | 10 | customer_name        | 고객 이름                | 자유 텍스트 (부분 일치) |
                | 11 | customer_phone       | 고객 연락처              | 자유 텍스트 (부분 일치) |
                | 12 | customer_type        | 고객 유형                | 개인 또는 법인 |
                | 13 | customer_grade       | 고객 등급 (OR, 복수 가능) | VVIP / VIP / DIAMOND |
                | 14 | risk_type            | 위험 유형 (OR, 복수 가능) | 폭언/욕설 / 해지위험 / 반복민원 / 사기의심 / 정책악용 / 과도한 보상 요구 / 피싱피해 |
                | 15 | product_name         | 상품명                   | 자유 텍스트 (부분 일치) |
                | 17 | consult_satisfaction | 고객만족도               | 숫자 (예: 4, 5) |
                """,
            example = "1"
        )
        @NotNull(message = "필터 ID는 필수입니다")
        private Integer filterId;

        @Schema(
            description = "필터 값. filterId에 따라 형식이 다름 (위 filterId 설명 참고)",
            example = "해지"
        )
        @NotBlank(message = "필터 값은 필수입니다")
        private String filterValue;
    }
}