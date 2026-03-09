package com.uplus.crm.domain.manual.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ManualRequest(
	    @Schema(description = "카테고리 정책 코드", example = "M_COUNSEL_01")
	    @NotBlank(message = "카테고리 코드는 필수입니다.")
	    String categoryCode,

	    @Schema(description = "매뉴얼 제목", example = "상담 인사말 채점 기준 V1")
	    @NotBlank(message = "제목은 필수입니다.")
	    String title,

	    @Schema(description = "매뉴얼 가이드 본문 내용", example = "고객 응대 시 특정 문구 포함 여부...")
	    @NotBlank(message = "내용은 필수입니다.")
	    String content
	) {}