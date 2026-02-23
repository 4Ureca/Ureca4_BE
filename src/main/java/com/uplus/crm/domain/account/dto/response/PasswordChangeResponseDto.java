package com.uplus.crm.domain.account.dto.response;
// PUT /auth/me/password — 비밀번호 변경
// Response
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeResponseDto {
    private String message;           // 결과 메시지
}
