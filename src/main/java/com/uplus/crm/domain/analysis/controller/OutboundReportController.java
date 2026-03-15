package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.domain.analysis.dto.outbound.*;
import com.uplus.crm.domain.analysis.service.OutboundReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 아웃바운드 대시보드 조회 API
 *
 * outbound_report_snapshot에서 집계된 데이터를 조회합니다.
 * period(daily/weekly/monthly) + date로 기간을 지정합니다.
 */
@Tag(name = "outbound_report", description = "아웃바운드 대시보드 분석 API")
@RestController
@RequestMapping("/analysis/outbound")
@RequiredArgsConstructor
public class OutboundReportController {

    private final OutboundReportService outboundReportService;

    @Operation(summary = "KPI 요약", description = "아웃바운드 KPI 5개 지표 (총 건수, 전환율, 평균 통화시간, 예상 매출 등)")
    @GetMapping("/kpi")
    public ResponseEntity<OutboundKpiResponse> getKpi(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getKpi(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "캠페인 성과 현황", description = "아웃바운드 카테고리별 캠페인 성과 테이블")
    @GetMapping("/campaigns")
    public ResponseEntity<OutboundCampaignResponse> getCampaigns(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getCampaigns(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "발신 결과 분포 + 거절 사유", description = "CONVERTED/REJECTED 분포 및 거절 사유별 건수")
    @GetMapping("/call-results")
    public ResponseEntity<OutboundCallResultResponse> getCallResults(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getCallResults(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "시간대×요일 전환율 히트맵", description = "시간대별, 요일별 전환율 히트맵 데이터")
    @GetMapping("/heatmap")
    public ResponseEntity<OutboundHeatmapResponse> getHeatmap(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getHeatmap(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "상담사별 실적", description = "상담사별 전환 건수, 전환율, 평균 통화시간 순위")
    @GetMapping("/agents")
    public ResponseEntity<OutboundAgentResponse> getAgents(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getAgents(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "카테고리별 최적 연락 시간", description = "카테고리별 전환율이 가장 높은 시간대 및 요일 추천")
    @GetMapping("/optimal-time")
    public ResponseEntity<OutboundOptimalTimeResponse> getOptimalTime(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getOptimalTime(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "카테고리별 전환율", description = "아웃바운드 카테고리별 전환 성공/전체 건수 및 전환율")
    @GetMapping("/conversion-by-category")
    public ResponseEntity<OutboundConversionResponse> getConversionByCategory(
            @Parameter(description = "기간: daily/weekly/monthly", example = "monthly")
            @RequestParam String period,
            @Parameter(description = "기준일 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return outboundReportService.getConversionByCategory(period, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
