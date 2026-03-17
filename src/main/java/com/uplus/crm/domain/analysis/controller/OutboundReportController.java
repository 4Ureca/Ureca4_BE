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

    @Operation(summary = "KPI 요약", description = "아웃바운드 상담의 핵심 성과 지표를 조회합니다.\n"
            + "- totalCount: 해당 기간 아웃바운드 상담 총 건수 (평일만 집계)\n"
            + "- convertedCount / rejectedCount: 전환 성공 / 거절 건수\n"
            + "- conversionRate: 전환율 (%) = convertedCount / totalCount × 100\n"
            + "- avgDurationSec: 전체 아웃바운드 상담의 평균 통화 시간 (초)\n"
            + "- estimatedRevenue: 전환 성공 상담에서 신규 가입(NEW)된 상품의 월정액 합산 예상 매출 (원)")
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

    @Operation(summary = "캠페인 성과 현황", description = "아웃바운드 카테고리(M_OTB_01~06)별 캠페인 성과를 조회합니다.\n"
            + "- categoryCode / categoryName: 아웃바운드 카테고리 코드 및 명칭\n"
            + "- totalCount: 해당 카테고리 상담 건수\n"
            + "- convertedCount / conversionRate: 전환 건수 및 전환율 (%)\n"
            + "- avgDurationSec: 평균 통화 시간 (초)\n"
            + "- avgSatisfiedScore: 고객 만족도 평균 (1~5점)\n"
            + "- estimatedRevenue: 해당 카테고리의 전환 상담 예상 매출 (원)\n"
            + "- isActive: 캠페인 활성 여부 (consultation_category_policy 기반)")
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

    @Operation(summary = "발신 결과 분포 + 거절 사유", description = "아웃바운드 상담의 발신 결과 분포와 거절 사유를 조회합니다.\n"
            + "- distribution: CONVERTED / REJECTED 건수\n"
            + "- rejectReasons: 거절 사유별 건수 (analysis_code 테이블에서 display_name 매핑)\n"
            + "  - code: 거절 사유 코드 (예: CONSIDER, PRICE 등)\n"
            + "  - name: 표시 명칭 (DB display_name)\n"
            + "  - description: 상세 설명 (DB description)\n"
            + "  - count: 건수")
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

    @Operation(summary = "시간대×요일 전환율 히트맵", description = "시간대(9~18시)와 요일(월~금)의 전환율 매트릭스를 조회합니다.\n"
            + "- hour: 시간대 (9~18)\n"
            + "- days: [월, 화, 수, 목, 금] 순서의 전환율 (%) 배열\n"
            + "- 각 셀 값 = 해당 시간대·요일의 전환 건수 / 총 건수 × 100\n"
            + "- 프론트엔드에서 히트맵 차트로 시각화하는 데 사용")
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

    @Operation(summary = "상담사별 실적", description = "아웃바운드 상담사별 실적 순위를 조회합니다.\n"
            + "- rank: 전환 건수 기준 순위\n"
            + "- agentId / agentName: 상담사 ID 및 이름\n"
            + "- totalCount: 해당 상담사의 아웃바운드 상담 총 건수\n"
            + "- convertedCount / conversionRate: 전환 건수 및 전환율 (%)\n"
            + "- avgDurationSec: 평균 통화 시간 (초)")
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

    @Operation(summary = "카테고리별 최적 연락 시간", description = "카테고리별 전환율이 가장 높은 시간대와 요일을 추천합니다.\n"
            + "- categoryCode / categoryName: 아웃바운드 카테고리\n"
            + "- bestHourRange: 전환율이 가장 높은 시간대 (예: '14:00 ~ 15:00')\n"
            + "- bestConversionRate: 해당 시간대의 전환율 (%)\n"
            + "- bestDays: 전환율이 높은 요일 상위 2개 (예: ['화', '목'])")
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

    @Operation(summary = "카테고리별 전환율", description = "아웃바운드 카테고리별 전환 성과를 조회합니다.\n"
            + "- categoryCode / categoryName: 아웃바운드 카테고리\n"
            + "- convertedCount: 전환 성공 건수\n"
            + "- totalCount: 총 상담 건수\n"
            + "- conversionRate: 전환율 (%) = convertedCount / totalCount × 100\n"
            + "- 전환율 내림차순 정렬")
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
