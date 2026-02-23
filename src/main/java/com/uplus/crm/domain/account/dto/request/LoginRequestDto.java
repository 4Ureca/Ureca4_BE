package com.uplus.crm.domain.account.dto.request;
//POST /auth/login — 일반 로그인
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LoginRequestDto {
    private String loginId;           // 로그인 ID
    private String password;          // 비밀번호
}
