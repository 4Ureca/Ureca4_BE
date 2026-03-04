package com.uplus.crm.domain.consultation.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.domain.consultation.dto.response.ConsultDataResponse;
import com.uplus.crm.domain.consultation.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consultation", description = "상담 결과서 API")
@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @Operation(summary = "랜덤 상담 결과서 조회",
               description = "DB에서 랜덤으로 1건을 선택해 고객정보 + 상담기본정보 + IAM 필드를 포함하여 반환합니다.")
    @GetMapping
    public ApiResponse<ConsultDataResponse> getRandomConsultData() {
        return ApiResponse.ok(consultationService.getRandomConsultData());
    }
}
