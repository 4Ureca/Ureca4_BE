package com.uplus.crm.domain.demo.dto.request;

import com.uplus.crm.domain.demo.dto.response.DemoSubscribedProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(
        description = "상담 결과 제출 요청 — GET /demo/consultation 응답값을 그대로 채워 보내고, iamIssue/iamAction/iamMemo만 직접 입력합니다.",
        example = """
                {
                  "customerId": 3281,
                  "channel": "CHATTING",
                  "categoryCode": "M_CHN_02",
                  "durationSec": 184,
                  "subscribedProducts": [
                    {
                      "productType": "MOBILE",
                      "productCode": "MOB-5G-YST",
                      "productName": "5G 유쓰 스탠다드",
                      "category": "5G 유쓰"
                    },
                    {
                      "productType": "ADDITIONAL",
                      "productCode": "ADD-MEMBERSHIP",
                      "productName": "U+멤버십",
                      "category": "멤버십"
                    }
                  ],
                  "iamIssue": "IPTV 해지 요청 및 위약금 문의",
                  "iamAction": "IPTV 베이직 채널 상품으로 변경 처리",
                  "iamMemo": "결합 할인 유지, 다음 달 청구서부터 반영",
                  "rawTextJson": "[{\\"text\\": \\"안녕하세요. LG U+ 채팅 상담입니다. 문의 사항을 입력해 주세요.\\", \\"speaker\\": \\"상담사\\"}, {\\"text\\": \\"인터넷만 쓰고 IPTV는 해지하고 싶어요.\\", \\"speaker\\": \\"고객\\"}, {\\"text\\": \\"IPTV 해지 시 결합 할인이 소멸됩니다. 저가 채널 상품으로 변경하시면 더 유리합니다.\\", \\"speaker\\": \\"상담사\\"}, {\\"text\\": \\"베이직으로 변경해주세요.\\", \\"speaker\\": \\"고객\\"}, {\\"text\\": \\"처리 완료하겠습니다. 다음 달 청구서부터 반영됩니다.\\", \\"speaker\\": \\"상담사\\"}]"
                }
                """
)
public record DemoConsultSubmitRequest(

        @Schema(description = "고객 식별자 (GET 응답의 customerId)", example = "3281")
        @NotNull Long customerId,

        @Schema(description = "상담 채널 (GET 응답의 channel)", allowableValues = {"CALL", "CHATTING"}, example = "CHATTING")
        @NotBlank String channel,

        @Schema(description = "상담 카테고리 코드 (GET 응답의 categoryCode)", example = "M_CHN_02")
        @NotBlank String categoryCode,

        @Schema(description = "상담 소요 시간(초) (GET 응답의 durationSec)", example = "184")
        @Positive int durationSec,

        @Schema(description = "고객 가입 상품 목록 (GET 응답의 subscribedProducts 그대로)")
        List<DemoSubscribedProduct> subscribedProducts,

        @Schema(description = "IAM 이슈 요약 — 상담사가 직접 입력", example = "IPTV 해지 요청 및 위약금 문의")
        String iamIssue,

        @Schema(description = "IAM 조치 내용 — 상담사가 직접 입력", example = "IPTV 베이직 채널 상품으로 변경 처리")
        String iamAction,

        @Schema(description = "IAM 메모 — 상담사가 직접 입력", example = "결합 할인 유지, 다음 달 청구서부터 반영")
        String iamMemo,

        @Schema(description = "상담 원문 JSON (GET 응답의 rawTextJson 그대로)", example = "[{\"text\": \"안녕하세요. LG U+ 채팅 상담입니다.\", \"speaker\": \"상담사\"}, {\"text\": \"IPTV 해지하고 싶어요.\", \"speaker\": \"고객\"}]")
        String rawTextJson
) {
}
