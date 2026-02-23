package com.uplus.crm.domain.account.dto.response;
//POST /auth/logout — 로그아웃 (HttpOnly Cookie)
//HttpOnly Cookie 방식이므로 RequestDto 불필요
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponseDto {
    private String message;           // 결과 메시지
}
