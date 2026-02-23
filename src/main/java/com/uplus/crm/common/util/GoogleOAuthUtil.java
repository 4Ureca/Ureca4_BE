package com.uplus.crm.common.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.uplus.crm.common.config.GoogleOAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleOAuthUtil {

    private final GoogleOAuthConfig googleOAuthConfig;

    public String getEmailFromAuthCode(String authorizationCode, String redirectUri) {
        try {
            // 1. Authorization Code로 Token 요청
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    googleOAuthConfig.getClientId(),
                    googleOAuthConfig.getClientSecret(),
                    authorizationCode,
                    redirectUri
            ).execute();

            // 2. ID Token에서 이메일 추출
            GoogleIdToken idToken = tokenResponse.parseIdToken();
            GoogleIdToken.Payload payload = idToken.getPayload();

            return payload.getEmail();

        } catch (IOException e) {
            throw new RuntimeException("Google OAuth 인증 실패: " + e.getMessage());
        }
    }
}