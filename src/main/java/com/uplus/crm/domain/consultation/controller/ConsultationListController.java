package com.uplus.crm.domain.consultation.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.domain.consultation.dto.response.ConsultationListResponseDto;
import com.uplus.crm.domain.consultation.service.ConsultationListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consultation List", description = "상담 내역 목록 조회 API")
@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
public class ConsultationListController {

    private final ConsultationListService consultationListService;

    @Operation(summary = "상담 내역 목록 조회", description = "상담 내역 목록을 검색/필터/페이징하여 조회합니다.")
    @GetMapping("/list")
    public ApiResponse<ConsultationListResponseDto> getConsultationList(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "channel", required = false, defaultValue = "") String channel,
            @RequestParam(name = "categoryCode", required = false, defaultValue = "") String categoryCode,
            @RequestParam(name = "summaryStatus", required = false, defaultValue = "") String summaryStatus,
            @RequestParam(name = "resultStatus", required = false, defaultValue = "") String resultStatus,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                consultationListService.getConsultationList(
                        keyword,
                        channel,
                        categoryCode,
                        summaryStatus,
                        resultStatus,
                        page,
                        size
                )
        );
    }
}