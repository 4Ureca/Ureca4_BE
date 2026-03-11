package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.CategorySummaryResponse;
import com.uplus.crm.domain.analysis.dto.ChurnDefenseResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
import com.uplus.crm.domain.analysis.dto.SubscriptionAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.TimeSlotTrendResponse;
import com.uplus.crm.domain.analysis.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 관리자 리포트 통합 조회 API
 *
 * period(daily/weekly/monthly) path variable로 기간을 지정하고,
 * 기능별 엔드포인트에서 해당 기간의 리포트를 조회합니다.
 */
@Tag(name = "admin_report", description = "관리자 분석 리포트 조회 API (기간별 통합)")
@RestController
@RequestMapping("/analysis/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // ==================== 성과 ====================

    @Operation(
            summary = "전체 상담 성과 요약",
            description = "지정 기간의 전체 상담 성과 요약을 조회합니다.\n\n"
                    + "**제공 지표:**\n"
                    + "- totalConsultCount: 전체 상담 처리 건수\n"
                    + "- avgConsultCountPerAgent: 상담사 1인당 평균 처리 건수\n"
                    + "- avgDurationMinutes: 상담 평균 소요 시간(분)\n"
                    + "- avgSatisfiedScore: 평균 고객 만족도 (5점 만점)\n\n"
                    + "**집계 방식:**\n"
                    + "- daily: 당일 전체 상담사의 개별 스냅샷을 실시간 집계\n"
                    + "- weekly/monthly: 배치가 사전 집계한 스냅샷 조회\n\n"
                    + "**지원 기간:** daily, weekly, monthly"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음 (배치 미실행 또는 해당 날짜 데이터 없음)", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily/weekly/monthly 외 값 입력 시 발생)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/performance")
    public ResponseEntity<PerformanceSummaryResponse> getPerformanceSummary(
            @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getPerformanceSummary(period, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "상담사 성과 순위 (TOP 10)",
            description = "지정 기간의 상담사별 종합 점수를 계산하여 상위 10명의 순위를 제공합니다.\n\n"
                    + "**상담사별 제공 지표:**\n"
                    + "- consultCount: 상담 처리 건수\n"
                    + "- avgDurationMinutes: 평균 소요 시간(분)\n"
                    + "- avgSatisfiedScore: 고객 만족도 (5점 만점)\n"
                    + "- qualityScore: 응대 품질 점수 (5점 만점)\n\n"
                    + "**종합 점수 산출 기준:**\n\n"
                    + "| 지표 | 측정 방식 | 가중치 |\n"
                    + "|------|---------|--------|\n"
                    + "| 처리 건수 | 건수 (Min-Max 정규화) | 25% |\n"
                    + "| 소요 시간 | 평균 소요 시간 (Min-Max 정규화, 역산) | 15% |\n"
                    + "| 응대 품질 | 품질 점수 정규화 (5점 → 0~1) | 30% |\n"
                    + "| 고객 만족도 | 만족도 정규화 (5점 → 0~1) | 30% |\n\n"
                    + "**종합 점수** = 가중 합산 (0~1 스케일)\n\n"
                    + "**지원 기간:** daily, weekly, monthly"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (상담사 데이터가 부족하면 10명 미만 반환 가능)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/agent-ranking")
    public ResponseEntity<AgentRankingResponse> getAgentRanking(
            @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getAgentRanking(period, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 고객 특이사항 ====================

    @Operation(
            summary = "고객 특이사항 조회",
            description = "고객 리스크 유형별 플래그 건수를 조회합니다.\n\n"
                    + "**7개 위험 유형:**\n"
                    + "- fraudSuspect(사기 의심), maliciousComplaint(악성 민원), policyAbuse(정책 악용)\n"
                    + "- excessiveCompensation(과도 보상 요구), repeatedComplaint(반복 민원)\n"
                    + "- phishingVictim(피싱 피해), churnRisk(해지 위험)\n\n"
                    + "**급증 경고 (daily only):**\n"
                    + "- surgeAlerts: 전일 대비 총 건수 증가율이 50% 이상이거나, 개별 유형이 전일 대비 2배 이상이면 경고 발생\n"
                    + "- surgeTypes: 급증이 감지된 유형 코드 목록 (FRAUD, ABUSE, POLICY 등)\n"
                    + "- 전일 스냅샷이 없으면 surgeAlerts는 null\n\n"
                    + "**지원 기간:** daily, monthly (weekly 미지원)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (surgeAlerts가 null이면 전일 스냅샷이 없어 급증 비교 불가)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (weekly 미지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/customer-risk")
    public ResponseEntity<CustomerRiskResponse> getCustomerRisk(
            @Parameter(description = "조회 주기 (daily, monthly)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        CustomerRiskResponse response = adminReportService.getCustomerRisk(period, targetDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 특이사항 기간 비교",
            description = "두 날짜의 고객 위험 스냅샷을 비교하여 유형별 증감을 분석합니다.\n\n"
                    + "**제공 데이터:**\n"
                    + "- base/compare: 각 날짜의 7개 위험 유형별 건수\n"
                    + "- changes: 유형별 증감 건수(diff)와 변화율(changeRate, %)\n"
                    + "- surgeDetected: 급증 감지 여부 (총 변화율 ≥50% 또는 개별 유형 2배 이상)\n"
                    + "- surgeTypes: 급증 감지된 유형 코드 목록\n\n"
                    + "**주의:** 두 날짜 모두 스냅샷이 존재해야 합니다. 하나라도 없으면 204 반환.\n\n"
                    + "**지원 기간:** daily only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비교 성공"),
            @ApiResponse(responseCode = "204", description = "기준일 또는 비교일 스냅샷 없음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/customer-risk/compare")
    public ResponseEntity<CustomerRiskCompareResponse> compareCustomerRisk(
            @Parameter(description = "조회 주기 (daily)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
            @Parameter(description = "비교 날짜 (yyyy-MM-dd)", example = "2025-01-10")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate compareDate) {

        CustomerRiskCompareResponse response = adminReportService.compareCustomerRisk(period, baseDate, compareDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    // ==================== 시간대별 트렌드 (daily only) ====================

    @Operation(
            summary = "시간대별 이슈 트렌드",
            description = "3시간 단위 슬롯별 상담 트렌드를 조회합니다.\n\n"
                    + "**슬롯별 제공 지표:**\n"
                    + "- consultCount: 해당 시간대 상담 건수\n"
                    + "- avgDuration: 평균 소요 시간(분)\n"
                    + "- categoryBreakdown: 카테고리별 건수 및 비율\n\n"
                    + "**슬롯 파라미터:**\n"
                    + "- 특정 슬롯 선택 시 해당 시간대만 반환\n"
                    + "- 미지정 시 배치가 집계한 전체 슬롯 데이터 반환\n"
                    + "- 해당 슬롯에 상담 이력이 없으면 빈 배열 반환\n\n"
                    + "**지원 기간:** daily only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (timeSlotTrend가 빈 배열이면 해당 슬롯에 상담 이력 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 날짜에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/time-slot-trend")
    public ResponseEntity<TimeSlotTrendResponse> getTimeSlotTrend(
            @Parameter(description = "조회 주기 (daily)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (미지정 시 전체 슬롯)",
                    schema = @Schema(allowableValues = {"09-12", "12-15", "15-18"}))
            @RequestParam(required = false) String slot) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getTimeSlotTrend(period, targetDate, slot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 카테고리 요약 (daily only) ====================

    @Operation(
            summary = "카테고리별 빈도",
            description = "상담 카테고리(대분류)별 건수와 비율을 조회합니다.\n\n"
                    + "**제공 지표:**\n"
                    + "- code: 카테고리 코드 (FEE, TRB 등)\n"
                    + "- name: 카테고리명 (요금/납부, 장애/A/S 등)\n"
                    + "- count: 해당 카테고리 상담 건수\n"
                    + "- rate: 전체 대비 비율 (%)\n\n"
                    + "**슬롯별 조회:** 특정 슬롯 선택 시 해당 시간대의 카테고리만 집계, 미지정 시 전체 집계\n\n"
                    + "**지원 기간:** daily only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (categorySummary가 빈 배열이면 해당 슬롯/날짜에 카테고리 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 날짜에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/category-summary")
    public ResponseEntity<CategorySummaryResponse> getCategorySummary(
            @Parameter(description = "조회 주기 (daily)", example = "daily")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (미지정 시 전체 슬롯)",
                    schema = @Schema(allowableValues = {"09-12", "12-15", "15-18"}))
            @RequestParam(required = false) String slot) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getCategorySummary(period, targetDate, slot)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 키워드 ====================

    @Operation(
            summary = "키워드 빈도 순위",
            description = "상담 내용에서 추출한 키워드 빈도 순위(TOP 20)를 조회합니다.\n\n"
                    + "**daily 응답 (KeywordRankingResponse):**\n"
                    + "- topKeywords: 상위 키워드 (keyword, count, rank, changeRate)\n"
                    + "- newKeywords: 신규 등장 키워드 (keyword, count)\n"
                    + "- slot 지정 시 해당 시간대 키워드, 미지정 시 일별 전체 키워드\n\n"
                    + "**weekly/monthly 응답 (KeywordAnalysisResponse):**\n"
                    + "- topKeywords: 기간 내 상위 키워드 (slot 파라미터 무시)\n\n"
                    + "**지원 기간:** daily, weekly, monthly"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (topKeywords가 빈 배열이면 해당 기간/슬롯에 키워드 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/keywords/top")
    public ResponseEntity<?> getKeywordTop(
            @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "시간대 슬롯 (daily만 지원, 미지정 시 전체)",
                    schema = @Schema(allowableValues = {"09-12", "12-15", "15-18"}))
            @RequestParam(required = false) String slot) {

        LocalDate targetDate = resolveDate(period, date);
        Object result = adminReportService.getKeywordTop(period, targetDate, slot);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "장기 상위 유지 키워드",
            description = "주간/월간 기간 동안 상위권을 지속 유지한 키워드를 조회합니다.\n\n"
                    + "**제공 지표 (KeywordAnalysisResponse):**\n"
                    + "- longTermTopKeywords: 장기 유지 키워드 목록\n"
                    + "  - keyword: 키워드명\n"
                    + "  - count: 기간 내 총 등장 횟수\n"
                    + "  - rank: 현재 순위\n"
                    + "  - appearanceDays: 상위권 유지 일수\n"
                    + "  - totalDays: 전체 집계 일수\n\n"
                    + "**지원 기간:** weekly, monthly (daily 미지원)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (longTermTopKeywords가 빈 배열이면 해당 기간 장기 유지 키워드 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily 미지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/keywords/long-term")
    public ResponseEntity<?> getKeywordLongTerm(
            @Parameter(description = "조회 주기 (weekly, monthly)", example = "weekly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        Object result = adminReportService.getKeywordLongTerm(period, targetDate);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "고객 유형별 키워드",
            description = "고객 등급(VIP, GOLD 등)별로 빈출 키워드를 조회합니다.\n\n"
                    + "**제공 지표:**\n"
                    + "- byCustomerType: 고객 유형별 키워드 목록\n"
                    + "  - customerType: 고객 등급 코드\n"
                    + "  - keywords: 해당 등급에서 빈출하는 키워드 리스트\n\n"
                    + "**지원 기간:** daily, weekly, monthly"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (byCustomerType가 빈 배열이면 해당 기간 고객 유형별 키워드 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/keywords/customer-types")
    public ResponseEntity<?> getKeywordCustomerTypes(
            @Parameter(description = "조회 주기 (daily, weekly, monthly)", example = "weekly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        Object result = adminReportService.getKeywordCustomerTypes(period, targetDate);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    // ==================== 구독상품 (weekly, monthly) ====================

    @Operation(
            summary = "상품별 신규가입/해지",
            description = "기간 내 신규 가입 및 해지 상위 상품(TOP 6)을 조회합니다.\n\n"
                    + "**제공 지표 (SubscriptionAnalysisResponse):**\n"
                    + "- newSubscriptions: 신규 가입 상위 6개 상품\n"
                    + "  - productId: 상품 코드\n"
                    + "  - productName: 상품명\n"
                    + "  - count: 가입 건수\n"
                    + "- canceledSubscriptions: 해지 상위 6개 상품\n"
                    + "  - productId, productName, count\n\n"
                    + "**집계 방식:** 배치가 상담 내 구독 변동을 집계하여 스냅샷 저장\n\n"
                    + "**지원 기간:** weekly, monthly (daily 미지원)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (리스트가 빈 배열이면 해당 기간 가입/해지 이력 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily 미지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/subscription/products")
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionProducts(
            @Parameter(description = "조회 주기 (weekly, monthly)", example = "weekly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getSubscriptionProducts(period, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "연령대별 상품 선호도",
            description = "연령대별 선호 상품(TOP 3)을 조회합니다.\n\n"
                    + "**제공 지표 (SubscriptionAnalysisResponse):**\n"
                    + "- byAgeGroup: 연령대별 선호 상품\n"
                    + "  - ageGroup: 연령대 (20대, 30대 등)\n"
                    + "  - preferredProducts: 해당 연령대 상위 3개 상품 (productId, productName, count)\n\n"
                    + "**집계 방식:** 배치가 고객 연령대와 구독 상품을 교차 집계\n\n"
                    + "**지원 기간:** weekly, monthly (daily 미지원)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (byAgeGroup이 빈 배열이면 해당 기간 연령대별 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 기간에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (daily 미지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/subscription/age-groups")
    public ResponseEntity<SubscriptionAnalysisResponse> getSubscriptionAgeGroups(
            @Parameter(description = "조회 주기 (weekly, monthly)", example = "weekly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        return adminReportService.getSubscriptionAgeGroups(period, targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ==================== 해지방어 (monthly only) ====================

    @Operation(
            summary = "해지방어 요약",
            description = "월간 해지방어 시도 건수, 성공 건수, 성공률, 평균 소요시간을 조회합니다.\n\n"
                    + "**제공 지표 (ChurnDefenseResponse):**\n"
                    + "- totalAttempts: 해지방어 총 시도 건수\n"
                    + "- successCount: 방어 성공 건수\n"
                    + "- successRate: 방어 성공률 (%)\n"
                    + "- avgDurationSec: 해지방어 상담 평균 소요 시간(초)\n\n"
                    + "**집계 방식:** 배치가 월간 해지 상담을 시도/성공으로 분류하여 스냅샷 저장\n\n"
                    + "**지원 기간:** monthly only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (totalAttempts가 0이면 해당 월 해지방어 시도 이력 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 월에 집계된 스냅샷이 존재하지 않음 (배치 미실행 또는 해당 월 데이터 없음)", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (monthly만 지원, daily/weekly 요청 시 발생)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/churn-defense/summary")
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseSummary(
            @Parameter(description = "조회 주기 (monthly)", example = "monthly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        ChurnDefenseResponse response = adminReportService.getChurnDefenseSummary(period, targetDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "불만 사유별 방어율",
            description = "고객 불만 사유별 해지방어 시도/성공/성공률을 조회합니다.\n\n"
                    + "**제공 지표 (ChurnDefenseResponse):**\n"
                    + "- complaintReasons: 사유별 방어 분석\n"
                    + "  - reason: 불만 사유 (요금 불만, 서비스 품질 등)\n"
                    + "  - attempts: 해당 사유 시도 건수\n"
                    + "  - successCount: 성공 건수\n"
                    + "  - successRate: 성공률 (%)\n"
                    + "  - avgDurationSec: 평균 소요 시간(초)\n\n"
                    + "**지원 기간:** monthly only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (complaintReasons가 빈 배열이면 해당 월 불만 사유 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 월에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (monthly만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/churn-defense/complaint-reasons")
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseComplaintReasons(
            @Parameter(description = "조회 주기 (monthly)", example = "monthly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        ChurnDefenseResponse response = adminReportService.getChurnDefenseComplaintReasons(period, targetDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 유형별 해지 분석",
            description = "고객 유형(연령대+성별)별 해지 시도 및 방어 성공률을 조회합니다.\n\n"
                    + "**제공 지표 (ChurnDefenseResponse):**\n"
                    + "- byCustomerType: 유형별 해지 분석\n"
                    + "  - type: 고객 유형 (예: 30대_남성)\n"
                    + "  - mainComplaintReason: 해당 유형의 주요 불만 사유\n"
                    + "  - attempts: 해지 시도 건수\n"
                    + "  - successRate: 방어 성공률 (%)\n\n"
                    + "**지원 기간:** monthly only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (byCustomerType가 빈 배열이면 해당 월 고객 유형별 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 월에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (monthly만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/churn-defense/customer-types")
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseCustomerTypes(
            @Parameter(description = "조회 주기 (monthly)", example = "monthly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        ChurnDefenseResponse response = adminReportService.getChurnDefenseCustomerTypes(period, targetDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "상담사 액션 분석",
            description = "해지방어 시 상담사가 수행한 액션별 시도 건수와 성공률을 조회합니다.\n\n"
                    + "**제공 지표 (ChurnDefenseResponse):**\n"
                    + "- byAction: 액션별 방어 분석\n"
                    + "  - action: 방어 액션 (요금할인, 부가서비스 제공 등)\n"
                    + "  - attempts: 해당 액션 시도 건수\n"
                    + "  - successRate: 성공률 (%)\n\n"
                    + "**지원 기간:** monthly only"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (byAction이 빈 배열이면 해당 월 액션별 데이터 없음)"),
            @ApiResponse(responseCode = "204", description = "해당 월에 집계된 스냅샷이 존재하지 않음", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 주기 (monthly만 지원)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{period}/churn-defense/actions")
    public ResponseEntity<ChurnDefenseResponse> getChurnDefenseActions(
            @Parameter(description = "조회 주기 (monthly)", example = "monthly")
            @PathVariable String period,
            @Parameter(description = "조회 기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = resolveDate(period, date);
        ChurnDefenseResponse response = adminReportService.getChurnDefenseActions(period, targetDate);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    // ==================== Helper ====================

    /**
     * 기간별 기본 날짜 결정
     * - daily: 전일(어제)
     * - weekly/monthly: 오늘 (스냅샷 범위 조회이므로 오늘 날짜로 포함 검색)
     */
    private LocalDate resolveDate(String period, LocalDate date) {
        if (date != null) return date;
        return "daily".equalsIgnoreCase(period)
                ? LocalDate.now().minusDays(1)
                : LocalDate.now();
    }
}
