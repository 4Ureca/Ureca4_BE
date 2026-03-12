package com.uplus.crm.domain.manual.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "매뉴얼 내용 수정 요청 데이터")
public record ManualUpdateRequest(
    @Schema(description = "수정할 매뉴얼 제목", example = "상담 인사말 채점 기준 V2")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
    String title,

    @Schema(description = "수정할 매뉴얼 가이드 본문 내용", example = "고객 응대 시 '반갑습니다' 문구를 필수 포함해야 함")
    @NotBlank(message = "내용은 필수입니다.")
    String content
) {}