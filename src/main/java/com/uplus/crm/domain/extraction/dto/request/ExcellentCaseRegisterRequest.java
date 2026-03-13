package com.uplus.crm.domain.extraction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExcellentCaseRegisterRequest(
 @Schema(description = "관리자 선정 사유", example = "상담사가 고객의 불만을 침착하게 응대하고 재약정을 이끌어냄")
 String adminReason
) {}