package com.uplus.crm.domain.demo.dto.request;

import com.uplus.crm.domain.demo.dto.response.DemoSubscribedProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "상담 결과 제출 요청 — GET /demo/consultation 응답값을 그대로 채워 보내고, iamIssue/iamAction/iamMemo만 직접 입력합니다.")
public record DemoConsultSubmitRequest(

        @Schema(description = "고객 식별자 (GET 응답의 customerId)", example = "1001")
        @NotNull Long customerId,

        @Schema(description = "상담 채널 (GET 응답의 channel)", allowableValues = {"CALL", "CHATTING"}, example = "CALL")
        @NotBlank String channel,

        @Schema(description = "상담 카테고리 코드 (GET 응답의 categoryCode)", example = "A01-B02-C03")
        @NotBlank String categoryCode,

        @Schema(description = "상담 소요 시간(초) (GET 응답의 durationSec)", example = "300")
        @Positive int durationSec,

        @Schema(description = "고객 가입 상품 목록 (GET 응답의 subscribedProducts 그대로)")
        List<DemoSubscribedProduct> subscribedProducts,

        @Schema(description = "IAM 이슈 요약 — 상담사가 직접 입력", example = "고객이 요금 과다 청구 문의")
        String iamIssue,

        @Schema(description = "IAM 조치 내용 — 상담사가 직접 입력", example = "청구 내역 확인 후 이의신청 안내")
        String iamAction,

        @Schema(description = "IAM 메모 — 상담사가 직접 입력", example = "다음 달 재확인 필요")
        String iamMemo
) {
}
