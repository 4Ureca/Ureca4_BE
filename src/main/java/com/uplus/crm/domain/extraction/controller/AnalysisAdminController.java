package com.uplus.crm.domain.extraction.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.web.bind.annotation.*;

import com.uplus.crm.domain.extraction.dto.response.FailedAnalysisDto;
import com.uplus.crm.domain.extraction.service.AnalysisAdminService;
import com.uplus.crm.domain.extraction.service.AnalysisRetryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI 분석 관리 API", description = "AI 요약 및 채점 실패 건에 대한 조회 및 재처리 API")
@RestController
@RequestMapping("analysis")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class AnalysisAdminController {

    private final AnalysisAdminService adminService;
    private final AnalysisRetryService retryService;

    @Operation(summary = "실패한 통합 분석 목록 조회", 
               description = "요약 또는 채점이 3회 이상 실패한 목록을 통합하여 조회합니다.")
    @GetMapping("/failed")
    public ResponseEntity<List<FailedAnalysisDto>> getFailedTasks() {
        return ResponseEntity.ok(adminService.getIntegratedFailedList());
    }

    @Operation(summary = "실패 건 수동 재처리 요청", 
               description = "선택한 consultId 리스트의 상태를 REQUESTED로 초기화하여 재분석을 시도합니다.")
    @PostMapping("/retry")
    public ResponseEntity<String> retryFailedTasks(@RequestBody List<Long> consultIds) {
        if (consultIds == null || consultIds.isEmpty()) {
            return ResponseEntity.badRequest().body("재처리할 ID가 없습니다.");
        }
        if (consultIds.size() > 100) { 
            return ResponseEntity.badRequest().body("한 번에 최대 100건까지만 재처리 가능합니다.");
        }
        retryService.retryConsultations(consultIds);
        return ResponseEntity.ok(consultIds.size() + "건의 재처리가 요청되었습니다.");
    }
}