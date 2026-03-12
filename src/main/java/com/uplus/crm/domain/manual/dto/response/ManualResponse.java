package com.uplus.crm.domain.manual.dto.response;

import java.time.LocalDateTime;
import com.uplus.crm.domain.manual.entity.Manual;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "매뉴얼 상세 및 이력 조회 응답 데이터")
public record ManualResponse(
    @Schema(description = "매뉴얼 고유 식별자(PK)", example = "1")
    Integer manualId,

    @Schema(description = "카테고리 정책 코드", example = "M_COUNSEL_01")
    String categoryCode,

    @Schema(description = "전체 카테고리 명칭", example = "[상담 > 친절 > 인사말]")
    String categoryName, 

    @Schema(description = "매뉴얼 제목", example = "상담 인사말 채점 기준 V2")
    String title,

    @Schema(description = "매뉴얼 가이드 본문 내용")
    String content,

    @Schema(description = "현재 활성화 여부", example = "true")
    Boolean isActive,

    @Schema(description = "작성자 이름", example = "이승혁")
    String empName, 

    @Schema(description = "최종 수정 일시")
    LocalDateTime updatedAt
) {
    public static ManualResponse from(Manual manual) {
        var policy = manual.getCategoryPolicy();
        
        return new ManualResponse(
            manual.getManualId(),
            policy.getCategoryCode(),
            String.format("[%s > %s > %s]", 
                policy.getLargeCategory(), 
                policy.getMediumCategory(), 
                policy.getSmallCategory()),
            manual.getTitle(),
            manual.getContent(),
            manual.getIsActive(),
            manual.getEmployee().getName(),
            manual.getUpdatedAt()
        );
    }
}