package com.uplus.crm.domain.demo.dto.response;

import java.time.LocalDate;

public record DemoConsultDataResponse(
        // 고객 정보
        Long customerId,
        String customerName,
        String phone,
        String customerType,
        String gender,
        LocalDate birthDate,
        String gradeCode,
        String email,
        // 상담 기본 정보
        String channel,
        String categoryCode,
        String largeCategory,
        String mediumCategory,
        String smallCategory,
        int durationSec,
        // IAM (빈값으로 전달)
        String iamIssue,
        String iamAction,
        String iamMemo
) {
}
