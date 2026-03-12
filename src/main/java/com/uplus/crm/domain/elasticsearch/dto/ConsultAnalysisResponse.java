package com.uplus.crm.domain.elasticsearch.dto;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 응대품질 분석 / 키워드 분석 API 응답 DTO.
 *
 * <p>ES {@code consult-index} 조회 결과 + MySQL {@code consultation_raw_texts.raw_text_json} 병합.</p>
 */
@Schema(description = "응대품질·키워드 분석 결과")
public record ConsultAnalysisResponse(

        @Schema(description = "ES 문서 ID")
        String id,

        @Schema(description = "상담 ID (MySQL consultation_results.consult_id)", example = "1001")
        Long consultId,

        @Schema(description = "AI 요약 내용")
        String content,

        @Schema(description = "IAM 이슈")
        String iamIssue,

        @Schema(description = "IAM 조치")
        String iamAction,

        @Schema(description = "IAM 메모")
        String iamMemo,

        @Schema(description = "감정 분류", example = "NEGATIVE")
        String sentiment,

        @Schema(description = "위험도 점수 (0~100)", example = "75")
        Integer riskScore,

        @Schema(description = "우선순위", example = "HIGH")
        String priority,

        @Schema(description = "고객 ID")
        String customerId,

        @Schema(description = "고객명")
        String customerName,

        @Schema(description = "전화번호")
        String phone,

        @Schema(description = "상담 일시")
        LocalDateTime createdAt,

        @Schema(description = "인사말 포함 여부 (상담사 발화 기준)")
        Boolean hasGreeting,

        @Schema(description = "마무리 인사 포함 여부 (상담사 발화 기준)")
        Boolean hasFarewell,

        @Schema(description = "대화 원문 JSON (MySQL consultation_raw_texts.raw_text_json)",
                example = "[{\"text\": \"안녕하세요.\", \"speaker\": \"상담사\"}, {\"text\": \"문의드립니다.\", \"speaker\": \"고객\"}]")
        String rawTextJson

) {
    /** ConsultDoc + rawTextJson 으로 응답 객체 생성 */
    public static ConsultAnalysisResponse of(ConsultDoc doc, String rawTextJson) {
        return new ConsultAnalysisResponse(
                doc.getId(),
                doc.getConsultId(),
                doc.getContent(),
                doc.getIamIssue(),
                doc.getIamAction(),
                doc.getIamMemo(),
                doc.getSentiment(),
                doc.getRiskScore(),
                doc.getPriority(),
                doc.getCustomerId(),
                doc.getCustomerName(),
                doc.getPhone(),
                doc.getCreatedAt(),
                doc.getHasGreeting(),
                doc.getHasFarewell(),
                rawTextJson
        );
    }
}
