package com.uplus.crm.domain.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.common.config.SecurityConfig;
import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean EmployeeRepository employeeRepository; // JwtAuthFilter 의존성

    // ─────────────────────────────────────────────
    // POST /auth/google
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Google OAuth 로그인 성공 - 200 OK, accessToken 반환")
    void googleLogin_success() throws Exception {
        // given
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("valid-auth-code")
                .redirectUri("http://localhost:3000/auth/callback")
                .build();

        GoogleAuthResponseDto response = GoogleAuthResponseDto.builder()
                .accessToken("mocked.jwt.access.token")
                .expiredAt(LocalDateTime.now().plusHours(1))
                .isNewUser(false)
                .build();

        given(authService.googleLogin(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked.jwt.access.token"))
                .andExpect(jsonPath("$.isNewUser").value(false))
                .andExpect(jsonPath("$.expiredAt").exists());
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - authorizationCode 누락 → 400")
    void googleLogin_fail_missingAuthCode() throws Exception {
        // given - authorizationCode 없이 요청
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .redirectUri("http://localhost:3000/auth/callback")
                .build();

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors.authorizationCode").exists());
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - redirectUri 누락 → 400")
    void googleLogin_fail_missingRedirectUri() throws Exception {
        // given - redirectUri 없이 요청
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("valid-auth-code")
                .build();

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors.redirectUri").exists());
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - Google 인증 실패 → 500")
    void googleLogin_fail_googleAuthFailed() throws Exception {
        // given - Google API 호출 실패 (만료된 code, 잘못된 code 등)
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("expired-or-invalid-code")
                .redirectUri("http://localhost:3000/auth/callback")
                .build();

        given(authService.googleLogin(any(), any()))
                .willThrow(new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED));

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("GOOGLE_AUTH_FAILED"));
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - 연동된 계정 없음 → 404")
    void googleLogin_fail_accountNotLinked() throws Exception {
        // given - Google 인증은 성공했지만 DB에 해당 이메일 직원이 없는 경우
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("valid-auth-code")
                .redirectUri("http://localhost:3000/auth/callback")
                .build();

        given(authService.googleLogin(any(), any()))
                .willThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_LINKED));

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ACCOUNT_NOT_LINKED"));
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - redirectUri 불일치 → 500")
    void googleLogin_fail_redirectUriMismatch() throws Exception {
        // given - Google Cloud Console에 등록되지 않은 redirectUri 사용
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("valid-auth-code")
                .redirectUri("http://localhost:9999/wrong-callback")
                .build();

        given(authService.googleLogin(any(), any()))
                .willThrow(new BusinessException(ErrorCode.GOOGLE_AUTH_FAILED));

        // when & then
        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("GOOGLE_AUTH_FAILED"));
    }
}
