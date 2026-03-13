package com.uplus.crm.domain.consultation.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.domain.consultation.dto.response.ConsultationDetailResponseDto;
import com.uplus.crm.domain.consultation.service.ConsultationDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consultation Detail", description = "상담결과서 상세 조회 API")
@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
public class ConsultationDetailController {

    private final ConsultationDetailService consultationDetailService;

    @Operation(
            summary = "상담결과서 상세 조회",
            description = "consultId 기준으로 기본정보, IAM 내용, 원문 대화, AI 분석결과, 변경 이력을 조회합니다."
    )
    @GetMapping("/detail")
    public ApiResponse<ConsultationDetailResponseDto> getConsultationDetail(
            @Parameter(description = "상담 결과서 ID", example = "12")
            @RequestParam(name = "consultId") Long consultId
    ) {
        return ApiResponse.ok(consultationDetailService.getConsultationDetail(consultId));
    }
}
