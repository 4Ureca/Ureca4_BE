package com.uplus.crm.domain.analysis.controller;

import com.uplus.crm.common.exception.ErrorResponse;
import com.uplus.crm.domain.analysis.dto.QualityAnalysisResponse;
import com.uplus.crm.domain.analysis.service.QualityAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.List;

/**
 * 응대 품질 분석 조회 API
 *
 * 상담사별 응대 품질(공감/사과/마무리/친절/신속/정확/대기안내 + 종합점수)을
 * 일별/주별/월별로 조회합니다.
 *
 * agentId 미지정 시 전체 상담사 목록, 지정 시 해당 상담사만 반환합니다.
 */
@Tag(name = "agent_report", description = "상담사 개인 리포트 조회 API")
@RestController
@RequestMapping("/analysis/agent")
@RequiredArgsConstructor
public class QualityAnalysisController {

    private final QualityAnalysisService qualityAnalysisService;

    @Operation(
            summary = "일별 응대 품질 분석",
            description = """
                daily_agent_report_snapshot에서 응대 품질 분석 데이터를 조회합니다.

                - **agentId 지정**: 해당 상담사의 품질 분석 (단건, 204 = 데이터 없음)
                - **agentId 미지정**: 전체 상담사 품질 분석 (목록)

                ### 품질 분석 기준
                | 지표 | 측정 방식 | 가중치 |
                |------|----------|--------|
                | 공감 표현 | 건당 등장 횟수 합산 | 20% |
                | 사과 표현 | 포함 여부 (비율) | 10% |
                | 마무리 인사 | 포함 여부 (비율) | 10% |
                | 친절 표현 | 포함 여부 (비율) | 15% |
                | 신속 응대 | 포함 여부 (비율) | 15% |
                | 정확 응대 | 포함 여부 (비율) | 15% |
                | 대기 안내 | 포함 여부 (비율) | 15% |

                **종합 점수** = 가중 합산 × 5.0 (5점 만점)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = QualityAnalysisResponse.class)))),
            @ApiResponse(responseCode = "204", description = "해당 상담사의 스냅샷 또는 품질 분석 데이터 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/daily/quality")
    public ResponseEntity<?> getDailyQuality(
            @Parameter(description = "상담사 ID (미지정 시 전체 조회)", example = "1")
            @RequestParam(required = false) Long agentId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd). 미지정 시 전일", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now().minusDays(1);

        if (agentId != null) {
            return qualityAnalysisService.getDailyByAgent(agentId, targetDate)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }
        List<QualityAnalysisResponse> result = qualityAnalysisService.getDailyAll(targetDate);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "주별 응대 품질 분석",
            description = """
                weekly_agent_report_snapshot에서 응대 품질 분석 데이터를 조회합니다.
                date가 포함되는 주간 스냅샷(startAt <= date <= endAt)을 찾습니다.

                - **agentId 지정**: 해당 상담사의 품질 분석 (단건, 일별 가중 평균)
                - **agentId 미지정**: 전체 상담사 품질 분석 (목록)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = QualityAnalysisResponse.class)))),
            @ApiResponse(responseCode = "204", description = "해당 상담사의 스냅샷 또는 품질 분석 데이터 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/weekly/quality")
    public ResponseEntity<?> getWeeklyQuality(
            @Parameter(description = "상담사 ID (미지정 시 전체 조회)", example = "1")
            @RequestParam(required = false) Long agentId,
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 주간 스냅샷 조회", example = "2025-01-18")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        if (agentId != null) {
            return qualityAnalysisService.getWeeklyByAgent(agentId, targetDate)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }
        List<QualityAnalysisResponse> result = qualityAnalysisService.getWeeklyAll(targetDate);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "월별 응대 품질 분석",
            description = """
                monthly_agent_report_snapshot에서 응대 품질 분석 데이터를 조회합니다.
                date가 포함되는 월간 스냅샷(startAt <= date <= endAt)을 찾습니다.

                - **agentId 지정**: 해당 상담사의 품질 분석 (단건, 일별 가중 평균)
                - **agentId 미지정**: 전체 상담사 품질 분석 (목록)
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = QualityAnalysisResponse.class)))),
            @ApiResponse(responseCode = "204", description = "해당 상담사의 스냅샷 또는 품질 분석 데이터 없음",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/monthly/quality")
    public ResponseEntity<?> getMonthlyQuality(
            @Parameter(description = "상담사 ID (미지정 시 전체 조회)", example = "1")
            @RequestParam(required = false) Long agentId,
            @Parameter(description = "기준 날짜 (yyyy-MM-dd). 해당 월간 스냅샷 조회", example = "2025-01-15")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        if (agentId != null) {
            return qualityAnalysisService.getMonthlyByAgent(agentId, targetDate)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());
        }
        List<QualityAnalysisResponse> result = qualityAnalysisService.getMonthlyAll(targetDate);
        return ResponseEntity.ok(result);
    }
}
