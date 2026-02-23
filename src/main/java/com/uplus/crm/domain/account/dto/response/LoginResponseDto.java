package com.uplus.crm.domain.account.dto.response;
//POST /auth/login — 일반 로그인
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;       // 접근 토큰 (JWT)
    private String refreshToken;      // 리프레시 토큰
    private LocalDateTime expiredAt;  // access 만료시각
}
