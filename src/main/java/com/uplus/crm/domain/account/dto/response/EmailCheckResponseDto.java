package com.uplus.crm.domain.account.dto.response;
// GET /auth/google/email-check — Google 이메일 중복 확인
// QueryParameter: email (String, 필수) — @RequestParam으로 처리
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
// Response
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCheckResponseDto {
    private Boolean available;        // 사용 가능 여부
    private String email;             // 확인한 이메일
}
