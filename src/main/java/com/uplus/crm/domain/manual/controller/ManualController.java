package com.uplus.crm.domain.manual.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
        @ApiResponse(responseCode = "404", description = "카테고리 정책 코드를 찾을 수 없음")
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 매뉴얼이 존재하지 않음")
    })
    @PutMapping("/{manualId}")
    public ResponseEntity<Void> updateManual(
            @Parameter(description = "수정할 매뉴얼 ID", example = "1")
            @PathVariable("manualId") Integer manualId, 
            @Valid @RequestBody ManualUpdateRequest request
    ) {
        manualService.updateManual(manualId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 이력 조회", description = "카테고리 코드가 있으면 해당 카테고리만, 없으면 전체 이력을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "잘못된 카테고리 코드 입력 시")
    })
    @GetMapping("/history") 
    public ResponseEntity<List<ManualResponse>> getManualHistory(
            @Parameter(description = "필터링할 카테고리 코드 (선택)", example = "M_COUNSEL_01")
            @RequestParam(name = "categoryCode", required = false) String categoryCode 
    ) {
        return ResponseEntity.ok(manualService.getHistory(categoryCode));
    }

    @Operation(summary = "매뉴얼 수동 비활성화", description = "활성화된 매뉴얼을 사용 중지 상태로 변경합니다.")
    @PatchMapping("/{manualId}/deactivate") 
    public ResponseEntity<Void> deactivateManual(@PathVariable("manualId") Integer manualId) {
        manualService.deactivateManual(manualId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매뉴얼 수동 활성화", description = "비활성화된 매뉴얼을 활성화합니다. 동일 카테고리의 다른 매뉴얼은 자동 비활성화됩니다.")
    @PatchMapping("/{manualId}/activate")
    public ResponseEntity<Void> activateManual(@PathVariable("manualId") Integer manualId) {
        manualService.activateManual(manualId);
        return ResponseEntity.ok().build();
    }
}