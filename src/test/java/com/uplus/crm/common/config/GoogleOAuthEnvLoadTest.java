package com.uplus.crm.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * .env 파일에 Google OAuth 자격증명이 올바르게 설정되어 있는지 검증하는 테스트.
 *
 * <p>Spring Boot는 기본적으로 .env 파일을 읽지 않습니다.
 * 이를 위해 'me.paulschwarz:spring-dotenv' 라이브러리가 build.gradle에 추가되었습니다.
 * 이 라이브러리가 있어야 ${GOOGLE_CLIENT_ID} 같은 표현식이 .env 값을 참조합니다.</p>
 */
@DisplayName(".env 파일 Google OAuth 자격증명 검증")
class GoogleOAuthEnvLoadTest {

    private static final String FALLBACK_CLIENT_ID = "local-dev-google-client-id";
    private static final String FALLBACK_CLIENT_SECRET = "local-dev-google-client-secret";

    @Test
    @DisplayName("[.env] GOOGLE_CLIENT_ID가 실제 값으로 설정되어 있다")
    void envFile_hasRealGoogleClientId() throws IOException {
        Map<String, String> env = parseEnvFile();

        String clientId = env.get("GOOGLE_CLIENT_ID");

        assertThat(clientId)
                .as("GOOGLE_CLIENT_ID가 .env 파일에 실제 값으로 설정되어 있어야 합니다 (비어있거나 fallback 값이면 안 됩니다)")
                .isNotNull()
                .isNotBlank()
                .isNotEqualTo(FALLBACK_CLIENT_ID);
    }

    @Test
    @DisplayName("[.env] GOOGLE_CLIENT_SECRET이 실제 값으로 설정되어 있다")
    void envFile_hasRealGoogleClientSecret() throws IOException {
        Map<String, String> env = parseEnvFile();

        String clientSecret = env.get("GOOGLE_CLIENT_SECRET");

        assertThat(clientSecret)
                .as("GOOGLE_CLIENT_SECRET이 .env 파일에 실제 값으로 설정되어 있어야 합니다 (비어있거나 fallback 값이면 안 됩니다)")
                .isNotNull()
                .isNotBlank()
                .isNotEqualTo(FALLBACK_CLIENT_SECRET);
    }

    @Test
    @DisplayName("[.env] GOOGLE_CLIENT_ID 형식이 올바르다 (.apps.googleusercontent.com으로 끝남)")
    void envFile_googleClientId_hasCorrectFormat() throws IOException {
        Map<String, String> env = parseEnvFile();

        String clientId = env.get("GOOGLE_CLIENT_ID");

        assertThat(clientId)
                .as("GOOGLE_CLIENT_ID는 Google OAuth ID 형식이어야 합니다 (예: xxx.apps.googleusercontent.com)")
                .isNotNull()
                .endsWith(".apps.googleusercontent.com");
    }

    @Test
    @DisplayName("[.env] GOOGLE_CLIENT_SECRET 형식이 올바르다 (GOCSPX- 로 시작함)")
    void envFile_googleClientSecret_hasCorrectFormat() throws IOException {
        Map<String, String> env = parseEnvFile();

        String clientSecret = env.get("GOOGLE_CLIENT_SECRET");

        assertThat(clientSecret)
                .as("GOOGLE_CLIENT_SECRET은 GOCSPX- 로 시작해야 합니다")
                .isNotNull()
                .startsWith("GOCSPX-");
    }

    // ─────────────────────────────────────────────
    // 헬퍼: .env 파일 파싱
    // ─────────────────────────────────────────────

    private Map<String, String> parseEnvFile() throws IOException {
        // Gradle 테스트 실행 시 working directory = 프로젝트 루트
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");

        assertThat(envPath.toFile())
                .as(".env 파일이 프로젝트 루트에 존재해야 합니다 (경로: %s)", envPath.toAbsolutePath())
                .exists();

        Map<String, String> env = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(envPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 주석(#) 또는 빈 줄, = 없는 줄 건너뜀
                if (line.startsWith("#") || line.isEmpty() || !line.contains("=")) {
                    continue;
                }
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (!value.isEmpty()) {
                    env.put(key, value);
                }
            }
        }
        return env;
    }
}
