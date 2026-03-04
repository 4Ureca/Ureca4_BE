package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.analysis.service.DailyReportService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "report_snapshot", description = "일별/주별/월별 분석 리포트 조회 API (관리자 전용)")
@RestController
@RequestMapping("/analysis/daily")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @Operation(
            summary = "고객 특이사항 조회",
            description = "daily_report_snapshot의 customerRiskAnalysis 섹션을 조회합니다. "
                    + "매일 02:00 배치가 생성한 스냅샷 기반이며, 실시간 집계가 아닙니다. "
                    + "전일 스냅샷과 비교하여 급증 경고(surgeAlerts)를 API 조회 시점에 계산합니다. "
                    + "date 미지정 시 전일(어제) 기준으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "해당 날짜 스냅샷 없음 (배치 미실행 또는 데이터 없음)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk")
    public ResponseEntity<CustomerRiskResponse> getCustomerRisk(
            @Parameter(description = "조회 대상 날짜 (yyyy-MM-dd). 미지정 시 전일 기준", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);
        CustomerRiskResponse response = dailyReportService.getCustomerRisk(targetDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "고객 특이사항 기간 비교",
            description = "두 날짜의 daily_report_snapshot을 비교하여 유형별 증감율과 급증 여부를 계산합니다. "
                    + "관리자가 직접 기준일/비교일을 선택하여 상세 분석할 때 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비교 성공"),
            @ApiResponse(responseCode = "204", description = "기준일 또는 비교일 스냅샷 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 토큰 없음/만료)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 전용)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/customer-risk/compare")
    public ResponseEntity<CustomerRiskCompareResponse> compareCustomerRisk(
            @Parameter(description = "기준 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate baseDate,

            @Parameter(description = "비교 날짜 (yyyy-MM-dd)", example = "2025-01-10")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate compareDate) {

        CustomerRiskCompareResponse response = dailyReportService.compareCustomerRisk(baseDate, compareDate);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}