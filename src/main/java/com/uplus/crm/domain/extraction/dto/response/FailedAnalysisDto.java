package com.uplus.crm.domain.extraction.dto.response;

import com.uplus.crm.domain.extraction.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "AI 분석 실패 목록 통합 응답 객체")
@Getter
@NoArgsConstructor
public class FailedAnalysisDto {

    @Schema(description = "상담 고유 ID", example = "4981")
    private Long consultId;

    @Schema(description = "상담 카테고리 코드", example = "OTB_NORMAL")
    private String categoryCode;

    @Schema(description = "요약 작업 상태", example = "FAILED")
    private EventStatus summaryStatus;

    @Schema(description = "요약 실패 상세 사유", example = "AI 요약 응답 시간 초과(3분)")
    private String summaryFailReason;

    @Schema(description = "요약 재시도 횟수", example = "3")
    private Integer summaryRetryCount;

    @Schema(description = "채점 작업 상태", example = "FAILED")
    private EventStatus scoringStatus;

    @Schema(description = "채점 실패 상세 사유", example = "503 SERVICE_UNAVAILABLE")
    private String scoringFailReason;

    @Schema(description = "채점 재시도 횟수", example = "3")
    private Integer scoringRetryCount;

    @Schema(description = "최종 업데이트 시간", example = "2026-03-18 14:46:20")
    private String updatedAt;

    public FailedAnalysisDto(Long consultId, String categoryCode, 
                             Object summaryStatus, String summaryFailReason, Object summaryRetryCount,
                             Object scoringStatus, String scoringFailReason, Object scoringRetryCount,
                             Object updatedAt) {
        this.consultId = consultId;
        this.categoryCode = categoryCode;
        this.summaryStatus = (EventStatus) summaryStatus;
        this.summaryFailReason = summaryFailReason;
        this.summaryRetryCount = summaryRetryCount != null ? ((Number) summaryRetryCount).intValue() : 0;
        this.scoringStatus = (EventStatus) scoringStatus;
        this.scoringFailReason = scoringFailReason;
        this.scoringRetryCount = scoringRetryCount != null ? ((Number) scoringRetryCount).intValue() : 0;
        this.updatedAt = updatedAt != null ? updatedAt.toString() : null;
    }
}