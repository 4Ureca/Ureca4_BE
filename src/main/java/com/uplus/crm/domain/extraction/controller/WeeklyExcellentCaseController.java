package com.uplus.crm.domain.extraction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.extraction.dto.response.WeeklyCaseDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.WeeklyExcellentCaseResponse;
import com.uplus.crm.domain.extraction.service.WeeklyExcellentCaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Weekly Excellent Case - Board", description = "상담사용 주간 우수 사례 게시판 API")
@RestController
@RequestMapping("/excellent-cases")
@RequiredArgsConstructor
public class WeeklyExcellentCaseController {

    private final WeeklyExcellentCaseService weeklyService;

    @Operation(
        summary = "주간 우수 사례 게시판 목록 조회", 
        description = "연도와 주차를 기준으로 선정된 우수 사례 목록을 조회합니다. 파라미터가 없으면 전체 목록을 최신순으로 반환합니다."
    )
    @GetMapping
    public ResponseEntity<List<WeeklyExcellentCaseResponse>> getWeeklyBoard(
            @Parameter(description = "조회 연도 (예: 2026)", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "조회 주차 (1~52)", example = "11")
            @RequestParam(required = false) Integer week) {
        
        return ResponseEntity.ok(weeklyService.getWeeklyBoard(year, week));
    }

    @Operation(
        summary = "주간 우수 사례 게시판 상세 조회", 
        description = "게시판 리스트에서 선택한 상담의 상세 내용(대화 원문, 관리자 선정 사유 포함)을 조회합니다."
    )
    @GetMapping("/{consultId}")
    public ResponseEntity<WeeklyCaseDetailResponse> getBoardDetail(
            @Parameter(description = "상담 ID", example = "16")
            @PathVariable("consultId") Long consultId) {
        return ResponseEntity.ok(weeklyService.getWeeklyDetail(consultId));
    }
}