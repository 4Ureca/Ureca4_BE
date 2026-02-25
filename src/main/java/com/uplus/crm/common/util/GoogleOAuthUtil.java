package com.uplus.crm.common.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.uplus.crm.common.config.GoogleOAuthConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuthUtil {

    private final GoogleOAuthConfig googleOAuthConfig;

    public String getEmailFromAuthCode(String authorizationCode, String redirectUri) {
        try {
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    googleOAuthConfig.getClientId(),
                    googleOAuthConfig.getClientSecret(),
                    authorizationCode,
                    redirectUri
            ).execute();

            GoogleIdToken idToken = tokenResponse.parseIdToken();
            return idToken.getPayload().getEmail();

        } catch (IOException e) {
            log.error("Google OAuth 인증 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED);
        }
    }
}