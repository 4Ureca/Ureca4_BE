package com.uplus.crm.domain.demo.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.demo.dto.request.DemoConsultSubmitRequest;
import com.uplus.crm.domain.demo.dto.response.DemoConsultDataResponse;
import com.uplus.crm.domain.demo.dto.response.DemoConsultSubmitResponse;
import com.uplus.crm.domain.demo.service.DemoConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Demo", description = "시연용 상담 등록 API")
@RestController
@RequestMapping("/demo/consultation")
@RequiredArgsConstructor
public class DemoConsultationController {

    private final DemoConsultationService demoConsultationService;

    @Operation(summary = "랜덤 상담 데이터 조회",
               description = "DB에서 랜덤으로 1건을 선택해 고객정보 + 상담기본정보를 반환합니다. IAM 3필드는 null로 반환됩니다.")
    @GetMapping
    public ApiResponse<DemoConsultDataResponse> getRandomConsultData() {
        return ApiResponse.ok(demoConsultationService.getRandomConsultData());
    }

    @Operation(summary = "상담 결과 제출",
               description = """
                       GET /demo/consultation 응답에서 받은 값을 그대로 채우고,\s
                       iamIssue / iamAction / iamMemo 세 필드를 직접 입력하여 전송합니다.\s
                       consultation_results 테이블에 신규 row를 삽입합니다.\s
                       (empId는 JWT 토큰에서 자동 추출, channel은 CALL 또는 CHATTING만 허용)""")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DemoConsultSubmitResponse> submitConsult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DemoConsultSubmitRequest request
    ) {
        return ApiResponse.ok("상담이 등록되었습니다.",
                demoConsultationService.submitConsult(request, userDetails.getEmpId()));
    }
}
