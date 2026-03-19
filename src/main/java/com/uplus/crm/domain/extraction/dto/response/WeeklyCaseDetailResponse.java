package com.uplus.crm.domain.extraction.dto.response;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주간 우수 사례 상세 조회 응답")
public record WeeklyCaseDetailResponse(
    @Schema(description = "상담 ID", example = "16") 
    Long consultId,
    
    @Schema(description = "상담사 이름", example = "승혁") 
    String counselorName,
    
    @Schema(description = "카테고리명", example = "상품 업그레이드 제안") 
    String categoryName,
    
    @Schema(description = "AI 채점 점수", example = "95") 
    Integer score,
    
    @Schema(description = "AI 평가 상세 사유") 
    String evaluationReason,
    
    @Schema(description = "상담 요약 내용") 
    String summary,
    
    @Schema(description = "관리자 선정 사유", example = "고객에게 인사를 잘함...") 
    String adminReason, 
    
    @Schema(description = "선정 일시") 
    LocalDateTime selectedAt, 
    
    @Schema(description = "상담 대화 원문 (JSON String)") 
    String rawTextJson 
) {
}