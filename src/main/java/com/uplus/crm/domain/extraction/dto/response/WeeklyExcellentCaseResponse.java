package com.uplus.crm.domain.extraction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "주간 우수 상담 사례 게시판 응답 DTO")
public record WeeklyExcellentCaseResponse(
    
    @Schema(description = "우수 사례 스냅샷 ID (PK)", example = "1")
    Long snapshotId,
    
    @Schema(description = "상담 ID", example = "25")
    Long consultId,
    
    @Schema(description = "상담사 성함", example = "홍길동")
    String counselorName,
    
    @Schema(description = "상담 소카테고리", example = "모바일 해지 방어")
    String smallCategory,
    
    @Schema(description = "게시글 제목", example = "[2026년 11주차] 우수 상담 사례")
    String title,
    
    @Schema(description = "AI 상담 요약 내용", example = "고객이 타사 이동을 고려했으나 결합 할인을 안내하여 유지함")
    String rawSummary,
    
    @Schema(description = "AI 평가 점수", example = "95")
    Integer score,
    
    @Schema(description = "관리자 선정 사유", example = "침착한 대응으로 고객의 불만을 해소하고 재약정을 이끌어낸 모범 사례임")
    String adminReason,
    
    @Schema(description = "우수 사례 선정 일시", example = "2026-03-13T14:30:00")
    LocalDateTime selectedAt
) {}