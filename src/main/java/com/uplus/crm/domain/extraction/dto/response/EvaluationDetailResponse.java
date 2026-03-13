package com.uplus.crm.domain.extraction.dto.response;

import com.uplus.crm.domain.extraction.entity.SelectionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "우수 사례 상세 조회 응답")
public record EvaluationDetailResponse(
    @Schema(description = "상담 ID", example = "16") Long consultId,
    @Schema(description = "상담사 이름", example = "승혁") String counselorName,
    @Schema(description = "카테고리명", example = "상품 업그레이드 제안") String categoryName,
    @Schema(description = "AI 채점 점수", example = "95") Integer score,
    @Schema(description = "AI 평가 상세 사유", example = "고객의 니즈를 정확히 파악하여...") String evaluationReason,
    @Schema(description = "상담 요약 내용") String summary,
    @Schema(description = "현재 선정 상태") SelectionStatus selectionStatus,
    @Schema(description = "분석 생성 일시") LocalDateTime createdAt,
    @Schema(description = "상담 대화 원문 (JSON String)") String rawTextJson
) {}