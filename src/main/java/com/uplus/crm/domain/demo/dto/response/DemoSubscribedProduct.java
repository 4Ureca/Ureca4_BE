package com.uplus.crm.domain.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 가입 상품 정보")
public record DemoSubscribedProduct(

        @Schema(description = "상품 유형", allowableValues = {"HOME", "MOBILE", "ADDITIONAL"}, example = "MOBILE")
        String productType,

        @Schema(description = "상품 코드", example = "MOB-5G-YST")
        String productCode,

        @Schema(description = "상품명", example = "5G 유쓰 스탠다드")
        String productName,

        @Schema(description = "카테고리", example = "5G 유쓰")
        String category
) {
}
