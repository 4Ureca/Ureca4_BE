package com.uplus.crm.domain.extraction.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseRegisterRequest;
import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.service.ExcellentCaseAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Excellent Case", description = "관리자용 우수 사례 후보군 관리 API")
@RestController
@RequestMapping("/api/admin/excellent-cases") 
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class ExcellentCaseAdminController {

    private final ExcellentCaseAdminService adminService;

    @Operation(summary = "우수 사례 후보군 리스트 조회", 
               description = "상태값(PENDING, SELECTED 등)과 정렬 기준을 사용하여 후보 목록을 페이징 조회합니다.")
    @GetMapping("/candidates")
    public ResponseEntity<Page<EvaluationListResponse>> getCandidates(
            @ParameterObject ExcellentCaseSearchRequest searchRequest, 
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 개수", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(adminService.getCandidatePage(searchRequest, page, size));
    }
    
    @Operation(summary = "우수 사례 후보군 상세 조회", description = "특정 상담의 AI 평가 사유와 대화 원문을 조회합니다.")
    @GetMapping("/{consultId}")
    public ResponseEntity<EvaluationDetailResponse> getDetail(
            @Parameter(description = "상담 ID", example = "16")
            @PathVariable("consultId") Long consultId) {
        
        return ResponseEntity.ok(adminService.getDetail(consultId));
    }
    
    @Operation(summary = "우수 사례 최종 선정", description = "상담을 검토한 후 주간 우수 사례로 최종 등록합니다.")
    @PostMapping("/{consultId}/select")
    public ResponseEntity<String> selectExcellentCase(
            @PathVariable("consultId") Long consultId,
            @RequestBody ExcellentCaseRegisterRequest request) {
        
        //(true: 신규 등록, false: 이미 존재)
        boolean isNew = adminService.registerExcellentCase(consultId, request);
        
        if (isNew) {
            return ResponseEntity.ok("우수 사례로 성공적으로 등록되었습니다.");
        } else {
            return ResponseEntity.ok("이미 등록된 우수 사례입니다.");
        }
    }
    
    @Operation(summary = "우수 사례 후보 제외", description = "검토 결과 우수 사례로 부적합한 경우 제외 처리합니다.")
    @PatchMapping("/{consultId}/reject")
    public ResponseEntity<String> rejectExcellentCase(@PathVariable("consultId") Long consultId) {
        
        //(true: 신규 제외, false: 이미 제외됨)
        boolean isChanged = adminService.rejectExcellentCase(consultId);
        
        if (isChanged) {
            return ResponseEntity.ok("해당 상담이 우수 사례 후보에서 제외되었습니다.");
        } else {
            return ResponseEntity.ok("이미 제외 처리된 상담입니다.");
        }
    }
}