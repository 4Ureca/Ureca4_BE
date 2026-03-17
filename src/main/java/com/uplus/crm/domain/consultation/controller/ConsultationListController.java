package com.uplus.crm.domain.consultation.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.domain.consultation.dto.response.ConsultationListResponseDto;
import com.uplus.crm.domain.consultation.service.ConsultationListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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

    @Operation(
        summary = "상담 내역 목록 조회", 
        description = "상담 내역 목록을 검색/필터/페이징하여 조회합니다. 필터 조건에 한글 값을 입력하면 내부적으로 DB 규격에 맞게 변환되어 처리됩니다."
    )
    @Parameters({
        @Parameter(name = "keyword", description = "고객명, 전화번호, 상담 ID", example = "홍길동"),
        @Parameter(name = "channel", description = "상담 채널 (입력 가능 값: 전화, 채팅)", example = "전화"),
        @Parameter(name = "categoryCode", description = "상담 카테고리 코드", example = "C001"),
        @Parameter(name = "summaryStatus", description = "AI 요약 상태 (입력 가능 값: 요약완료, 요청중, 실패)", example = "요약완료"),
        @Parameter(name = "resultStatus", description = "처리 상태 (입력 가능 값: 처리중, 완료, 미완료, 요청중)", example = "완료"),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
        @Parameter(name = "size", description = "페이지당 데이터 개수", example = "10")
    })
    
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