package com.uplus.crm.domain.account.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PUT /auth/me/password — 비밀번호 변경
// Request
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PasswordChangeRequestDto {
    private String currentPassword;   // 현재 비밀번호
    private String newPassword;       // 새 비밀번호
    private String confirmPassword;   // 새 비밀번호 확인
}
