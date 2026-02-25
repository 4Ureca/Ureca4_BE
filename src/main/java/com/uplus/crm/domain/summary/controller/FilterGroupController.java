package com.uplus.crm.domain.summary.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupCreateRequest;
import com.uplus.crm.domain.summary.dto.FilterGroupDetailResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupListResponse;
import com.uplus.crm.domain.summary.dto.FilterGroupOrderRequest;
import com.uplus.crm.domain.summary.dto.FilterGroupUpdateRequest;
import com.uplus.crm.domain.summary.dto.FilterResponse;
import com.uplus.crm.domain.summary.service.FilterGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search Filter", description = "검색 조건 저장/조회/수정/삭제 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FilterGroupController {

    private final FilterGroupService filterGroupService;

    // ==================== Filter 정의 (테이블 18) ====================

    @Operation(
            summary = "필터 정의 목록 조회",
            description = "검색 폼 동적 생성용. filter 테이블(18)의 전체 필터 항목(filterKey, filterName 등)을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<FilterResponse>>> getFilterDefinitions() {
        List<FilterResponse> filters = filterGroupService.getFilterDefinitions();
        return ResponseEntity.ok(Map.of("filters", filters));
    }

    // ==================== FilterGroup CRUD (테이블 31, 32) ====================

    @Operation(
            summary = "검색 조건 저장",
            description = "현재 검색 폼의 조건을 이름 붙여서 저장합니다. "
                    + "filter_groups(31) 1건 + filter_custom(32) N건을 하나의 트랜잭션으로 생성합니다. "
                    + "같은 filterId가 여러 개면 OR 조건으로 해석됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패 (groupName 누락, filters 비어있음, 유효하지 않은 filterId)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/search-filters")
    public ResponseEntity<FilterGroupDetailResponse> createFilterGroup(
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody FilterGroupCreateRequest request) {

        FilterGroupDetailResponse response = filterGroupService.createFilterGroup(empId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "내 필터 그룹 목록 조회",
            description = "JWT의 emp_id 기준으로 본인이 저장한 필터 그룹 목록을 반환합니다. "
                    + "sort_order 기준 정렬. 하위 필터값은 포함하지 않고 filterCount만 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/search-filters")
    public ResponseEntity<Map<String, List<FilterGroupListResponse>>> getMyFilterGroups(
            @AuthenticationPrincipal Integer empId) {

        List<FilterGroupListResponse> groups = filterGroupService.getMyFilterGroups(empId);
        return ResponseEntity.ok(Map.of("groups", groups));
    }

    @Operation(
            summary = "필터 그룹 상세 조회",
            description = "저장된 조건 클릭 시 호출. filter_custom(32)의 전체 값 + filter(18)의 filterKey/filterName을 JOIN하여 반환합니다. "
                    + "프론트에서 이 데이터로 검색 폼의 값을 복원합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 그룹 접근",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 그룹",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search-filters/{id}")
    public ResponseEntity<FilterGroupDetailResponse> getFilterGroupDetail(
            @Parameter(description = "필터 그룹 ID") @PathVariable("id") Integer filterGroupId,
            @AuthenticationPrincipal Integer empId) {

        FilterGroupDetailResponse response =
                filterGroupService.getFilterGroupDetail(filterGroupId, empId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "필터 그룹 수정",
            description = "그룹 이름 변경 + 기존 filter_custom 전체 삭제 후 새 값으로 교체합니다 (전체 교체 방식). "
                    + "부분 수정이 아닌 전체 교체이므로, 변경하지 않는 조건도 포함해서 보내야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 그룹",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 그룹",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/search-filters/{id}")
    public ResponseEntity<FilterGroupDetailResponse> updateFilterGroup(
            @Parameter(description = "필터 그룹 ID") @PathVariable("id") Integer filterGroupId,
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody FilterGroupUpdateRequest request) {

        FilterGroupDetailResponse response =
                filterGroupService.updateFilterGroup(filterGroupId, empId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "필터 그룹 삭제",
            description = "filter_groups 레코드를 실제 삭제(hard delete)합니다. "
                    + "filter_custom 데이터도 cascade로 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 그룹",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 그룹",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/search-filters/{id}")
    public ResponseEntity<Map<String, String>> deleteFilterGroup(
            @Parameter(description = "필터 그룹 ID") @PathVariable("id") Integer filterGroupId,
            @AuthenticationPrincipal Integer empId) {

        filterGroupService.deleteFilterGroup(filterGroupId, empId);
        return ResponseEntity.ok(Map.of("message", "필터 그룹이 삭제되었습니다"));
    }

    @Operation(
            summary = "필터 그룹 정렬 순서 변경",
            description = "저장된 조건의 표시 순서를 변경합니다. "
                    + "본인 소유 그룹만 변경 가능하며, 다른 사용자의 그룹이 포함되면 403 에러가 발생합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "순서 변경 성공"),
            @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 그룹 포함",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/search-filters/order")
    public ResponseEntity<Map<String, String>> updateFilterGroupOrder(
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody FilterGroupOrderRequest request) {

        filterGroupService.updateFilterGroupOrder(empId, request);
        return ResponseEntity.ok(Map.of("message", "정렬 순서가 변경되었습니다"));
    }
}