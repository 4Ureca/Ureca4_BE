package com.uplus.crm.domain.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "랜덤 상담 데이터 응답")
public record DemoConsultDataResponse(

        @Schema(description = "고객 식별자", example = "1001")
        Long customerId,

        @Schema(description = "고객명", example = "홍길동")
        String customerName,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "고객 유형", example = "개인")
        String customerType,

        @Schema(description = "성별", example = "남성")
        String gender,

        @Schema(description = "생년월일", example = "1990-05-15")
        LocalDate birthDate,

        @Schema(description = "등급 코드", example = "VIP")
        String gradeCode,

        @Schema(description = "이메일", example = "hong@lgup.com")
        String email,

        @Schema(description = "현재 활성 가입 상품 목록 (해지되지 않은 상품)")
        List<DemoSubscribedProduct> subscribedProducts,

        @Schema(description = "상담 채널", allowableValues = {"CALL", "CHATTING"}, example = "CALL")
        String channel,

        @Schema(description = "상담 카테고리 코드", example = "A01-B02-C03")
        String categoryCode,

        @Schema(description = "대분류", example = "요금")
        String largeCategory,

        @Schema(description = "중분류", example = "청구")
        String mediumCategory,

        @Schema(description = "소분류", example = "과금오류")
        String smallCategory,

        @Schema(description = "상담 소요 시간(초)", example = "300")
        int durationSec,

        @Schema(description = "IAM 이슈 (null로 반환 — 상담사가 직접 입력)", example = "null")
        String iamIssue,

        @Schema(description = "IAM 조치 (null로 반환 — 상담사가 직접 입력)", example = "null")
        String iamAction,

        @Schema(description = "IAM 메모 (null로 반환 — 상담사가 직접 입력)", example = "null")
        String iamMemo,

        @Schema(description = "상담 원문 JSON", example = "{\"messages\":[...]}")
        String rawTextJson
) {
}
