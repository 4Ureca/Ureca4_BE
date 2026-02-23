package com.uplus.crm.domain.account.dto.request;

//POST /auth/google — Google OAuth 연동

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class GoogleAuthRequestDto {
    private String authorizationCode; // Google authorization code
    private String redirectUri;       // 리다이렉트 URI
}
