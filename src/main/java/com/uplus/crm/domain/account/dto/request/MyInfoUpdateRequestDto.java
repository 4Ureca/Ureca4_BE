package com.uplus.crm.domain.account.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// PUT /auth/me — 내 정보 편집
// Request
@Getter
@NoArgsConstructor
public class MyInfoUpdateRequestDto {
    private String name;              // 이름
    private String email;             // 이메일
    private String phone;             // 전화번호 (nullable)
    private String birth;             // 생년월일 (nullable)
    private String gender;            // 성별 (nullable)
}
