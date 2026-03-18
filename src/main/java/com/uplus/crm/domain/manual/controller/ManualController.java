package com.uplus.crm.domain.manual.controller;

import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.request.ManualUpdateRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.service.ManualService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Manual Management", description = "관리자용 매뉴얼(채점 기준) 관리 API")
@RestController
@RequestMapping("/admin/manuals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ManualController {

    private final ManualService manualService;

    @Operation(summary = "매뉴얼 신규 작성/교체", description = "새 매뉴얼을 등록합니다. 기존 활성화된 매뉴얼은 자동으로 비활성화됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
    })
    @PostMapping
    public ResponseEntity<Void> createManual(
            @Valid @RequestBody ManualRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        manualService.createManual(request, userDetails.getEmpId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "매뉴얼 내용 수정", description = "특정 매뉴얼의 제목과 본문 내용을 수정합니다.")
    @PutMapping("/{manualId}")
    public ResponseEntity<Void> updateManual(
            @Parameter(description = "수정할 매뉴얼 ID", example = "1")
            @PathVariable("manualId") Integer manualId, 
            @Valid @RequestBody ManualUpdateRequest request
    ) {
        manualService.updateManual(manualId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 이력 조회 (페이징 & 필터)", 
               description = "카테고리 및 활성 상태('활성화'/'비활성화')에 따라 이력을 조회합니다.")
    @GetMapping("/history") 
    public ResponseEntity<Page<ManualResponse>> getManualHistory(
            @Parameter(description = "카테고리 코드 (선택)", example = "M_ADD_01")
            @RequestParam(name = "categoryCode", required = false) String categoryCode,
            
            @Parameter(description = "상태 필터 ('활성화', '비활성화', 또는 '전체')", example = "활성화")
            @RequestParam(name = "status", required = false) String status, 
            
            @ParameterObject Pageable pageable 
    ) {
        Boolean isActive = mapStatusToBoolean(status);
        
        return ResponseEntity.ok(manualService.getHistory(categoryCode, isActive, pageable));
    }

    @Operation(summary = "매뉴얼 수동 비활성화")
    @PatchMapping("/{manualId}/deactivate") 
    public ResponseEntity<Void> deactivateManual(@PathVariable("manualId") Integer manualId) {
        manualService.deactivateManual(manualId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 수동 활성화")
    @PatchMapping("/{manualId}/activate")
    public ResponseEntity<Void> activateManual(@PathVariable("manualId") Integer manualId) {
        manualService.activateManual(manualId);
        return ResponseEntity.ok().build();
    }

    /**
     * 프론트엔드 한글 상태값을 Boolean으로 매핑하는 헬퍼 메서드 🥊
     */
    private Boolean mapStatusToBoolean(String status) {
        if ("활성화".equals(status)) {
            return true;
        }
        if ("비활성화".equals(status)) {
            return false;
        }
        // "전체", null, 빈 문자열, 혹은 정의되지 않은 값("abc" 등)은 모두 필터링 안 함(null) 처리
        return null; 
    }
}