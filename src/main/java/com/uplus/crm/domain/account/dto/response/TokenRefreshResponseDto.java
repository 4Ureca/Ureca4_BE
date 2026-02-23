package com.uplus.crm.domain.account.dto.response;
//POST /auth/refresh — 토큰 갱신 (HttpOnly Cookie)
//HttpOnly Cookie 방식이므로 RequestDto 불필요
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// Response
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponseDto {
    private String accessToken;       // 새 접근 토큰 (JWT)
    private String refreshToken;      // 새 리프레시 토큰
    private LocalDateTime expiredAt;  // access 만료시각
}
