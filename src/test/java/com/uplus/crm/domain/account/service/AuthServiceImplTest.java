package com.uplus.crm.domain.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.CookieUtil;
import com.uplus.crm.common.util.GoogleOAuthUtil;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.MyInfoUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.RefreshToken;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.RefreshTokenRepository;
import com.uplus.crm.domain.account.service.impl.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GoogleOAuthUtil googleOAuthUtil;
    @Mock private JwtUtil jwtUtil;
    @Mock private CookieUtil cookieUtil;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpServletResponse httpResponse;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        mockEmployee = Employee.builder()
                .empId(1)
                .loginId("EMP001")
                .password("encodedPassword")
                .name("홍길동")
                .email("hong@lgup.com")
                .isActive(true)
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /auth/google
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Google OAuth 로그인 성공")
    void googleLogin_success() {
        // given
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("auth_code_123")
                .redirectUri("https://localhost:3000/callback")
                .build();

        given(googleOAuthUtil.getEmailFromAuthCode(any(), any()))
                .willReturn("hong@lgup.com");
        given(employeeRepository.findByEmail("hong@lgup.com"))
                .willReturn(Optional.of(mockEmployee));
        given(jwtUtil.generateAccessToken(any(), any()))
                .willReturn("accessToken");
        given(jwtUtil.generateRefreshToken(any()))
                .willReturn("refreshToken");
        given(jwtUtil.getAccessTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusHours(1));
        given(jwtUtil.getRefreshTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusDays(7));

        // when
        GoogleAuthResponseDto response = authService.googleLogin(request, httpResponse);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getIsNewUser()).isFalse();
        assertThat(response.getExpiredAt()).isNotNull();
        then(cookieUtil).should().setRefreshTokenCookie(eq(httpResponse), eq("refreshToken"));
    }

    @Test
    @DisplayName("Google OAuth 로그인 실패 - 연동된 계정 없음")
    void googleLogin_fail_noAccount() {
        // given
        GoogleAuthRequestDto request = GoogleAuthRequestDto.builder()
                .authorizationCode("auth_code_123")
                .redirectUri("https://localhost:3000/callback")
                .build();

        given(googleOAuthUtil.getEmailFromAuthCode(any(), any()))
                .willReturn("unknown@lgup.com");
        given(employeeRepository.findByEmail("unknown@lgup.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.googleLogin(request, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_LINKED);
                });
    }

    // ─────────────────────────────────────────────
    // POST /auth/login
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("일반 로그인 성공")
    void login_success() {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .loginId("EMP001")
                .password("P@ssw0rd")
                .build();

        given(employeeRepository.findByLoginId("EMP001"))
                .willReturn(Optional.of(mockEmployee));
        given(passwordEncoder.matches("P@ssw0rd", "encodedPassword"))
                .willReturn(true);
        given(jwtUtil.generateAccessToken(any(), any()))
                .willReturn("accessToken");
        given(jwtUtil.generateRefreshToken(any()))
                .willReturn("refreshToken");
        given(jwtUtil.getAccessTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusHours(1));
        given(jwtUtil.getRefreshTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusDays(7));

        // when
        LoginResponseDto response = authService.login(request, httpResponse);

        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getExpiredAt()).isNotNull();
        then(cookieUtil).should().setRefreshTokenCookie(eq(httpResponse), eq("refreshToken"));
    }

    @Test
    @DisplayName("일반 로그인 실패 - 존재하지 않는 아이디")
    void login_fail_notFound() {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .loginId("UNKNOWN")
                .password("P@ssw0rd")
                .build();

        given(employeeRepository.findByLoginId("UNKNOWN"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                });
    }

    @Test
    @DisplayName("일반 로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        // given
        LoginRequestDto request = LoginRequestDto.builder()
                .loginId("EMP001")
                .password("wrongPassword")
                .build();

        given(employeeRepository.findByLoginId("EMP001"))
                .willReturn(Optional.of(mockEmployee));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
                });
    }

    // ─────────────────────────────────────────────
    // POST /auth/logout
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // given
        given(cookieUtil.extractRefreshToken(httpRequest))
                .willReturn(Optional.of("validRefreshToken"));

        // when
        LogoutResponseDto response = authService.logout(httpRequest, httpResponse);

        // then
        assertThat(response.getMessage()).isEqualTo("로그아웃 되었습니다.");
        then(refreshTokenRepository).should().deleteByRefreshToken("validRefreshToken");
        then(cookieUtil).should().clearRefreshTokenCookie(httpResponse);
    }

    @Test
    @DisplayName("로그아웃 실패 - 쿠키 없음")
    void logout_fail_noCookie() {
        // given
        given(cookieUtil.extractRefreshToken(httpRequest))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.logout(httpRequest, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.MISSING_TOKEN);
                });
    }

    // ─────────────────────────────────────────────
    // POST /auth/refresh
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_success() {
        // given
        given(cookieUtil.extractRefreshToken(httpRequest))
                .willReturn(Optional.of("validRefreshToken"));

        RefreshToken mockRefreshToken = RefreshToken.builder()
                .refreshTokenId(1)
                .employee(mockEmployee)
                .refreshToken("validRefreshToken")
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        given(refreshTokenRepository.findByRefreshToken("validRefreshToken"))
                .willReturn(Optional.of(mockRefreshToken));
        given(jwtUtil.generateAccessToken(any(), any()))
                .willReturn("newAccessToken");
        given(jwtUtil.generateRefreshToken(any()))
                .willReturn("newRefreshToken");
        given(jwtUtil.getAccessTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusHours(1));
        given(jwtUtil.getRefreshTokenExpiredAt())
                .willReturn(LocalDateTime.now().plusDays(7));

        // when
        TokenRefreshResponseDto response = authService.refresh(httpRequest, httpResponse);

        // then
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getExpiredAt()).isNotNull();
        then(cookieUtil).should().setRefreshTokenCookie(eq(httpResponse), eq("newRefreshToken"));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 Refresh Token")
    void refresh_fail_invalidToken() {
        // given
        given(cookieUtil.extractRefreshToken(httpRequest))
                .willReturn(Optional.of("invalidToken"));
        given(refreshTokenRepository.findByRefreshToken("invalidToken"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh(httpRequest, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
                });
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 Refresh Token")
    void refresh_fail_expiredToken() {
        // given
        given(cookieUtil.extractRefreshToken(httpRequest))
                .willReturn(Optional.of("expiredToken"));

        RefreshToken expiredToken = RefreshToken.builder()
                .refreshTokenId(1)
                .employee(mockEmployee)
                .refreshToken("expiredToken")
                .expiredAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        given(refreshTokenRepository.findByRefreshToken("expiredToken"))
                .willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh(httpRequest, httpResponse))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
                });
    }

    // ─────────────────────────────────────────────
    // PUT /auth/me/password
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .currentPassword("oldPass123!")
                .newPassword("newPass456!")
                .confirmPassword("newPass456!")
                .build();

        given(employeeRepository.findById(1))
                .willReturn(Optional.of(mockEmployee));
        given(passwordEncoder.matches("oldPass123!", "encodedPassword"))
                .willReturn(true);
        given(passwordEncoder.encode("newPass456!"))
                .willReturn("newEncodedPassword");

        // when
        PasswordChangeResponseDto response = authService.changePassword(1, request);

        // then
        assertThat(response.getMessage()).isEqualTo("비밀번호가 변경되었습니다.");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 직원 없음")
    void changePassword_fail_notFound() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .currentPassword("oldPass123!")
                .newPassword("newPass456!")
                .confirmPassword("newPass456!")
                .build();

        given(employeeRepository.findById(999))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.changePassword(999, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_fail_wrongCurrentPassword() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .currentPassword("wrongPass!")
                .newPassword("newPass456!")
                .confirmPassword("newPass456!")
                .build();

        given(employeeRepository.findById(1))
                .willReturn(Optional.of(mockEmployee));
        given(passwordEncoder.matches("wrongPass!", "encodedPassword"))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.changePassword(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURRENT_PASSWORD);
                });
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 확인 불일치")
    void changePassword_fail_passwordMismatch() {
        // given
        PasswordChangeRequestDto request = PasswordChangeRequestDto.builder()
                .currentPassword("oldPass123!")
                .newPassword("newPass456!")
                .confirmPassword("differentPass!")
                .build();

        given(employeeRepository.findById(1))
                .willReturn(Optional.of(mockEmployee));
        given(passwordEncoder.matches("oldPass123!", "encodedPassword"))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.changePassword(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
                });
    }

    // ─────────────────────────────────────────────
    // PUT /auth/me
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /auth/me - 내 정보 수정")
    class UpdateMyInfo {

        @Test
        @DisplayName("성공 - 모든 필드 수정")
        void success_allFields() {
            // given
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("김철수")
                    .email("new@lgup.com")
                    .phone("010-1234-5678")
                    .birth("1990-05-15")
                    .gender("M")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("new@lgup.com", 1)).willReturn(false);

            // when
            MyInfoUpdateResponseDto res = authService.updateMyInfo(1, req);

            // then
            assertThat(res.getEmpId()).isEqualTo(1);
            assertThat(res.getName()).isEqualTo("김철수");
            assertThat(res.getEmail()).isEqualTo("new@lgup.com");
            assertThat(res.getPhone()).isEqualTo("010-1234-5678");
            assertThat(res.getBirth()).isEqualTo("1990-05-15");
            assertThat(res.getGender()).isEqualTo("M");
        }

        @Test
        @DisplayName("성공 - 선택 필드 null 허용")
        void success_nullableFieldsOmitted() {
            // given
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);

            // when
            MyInfoUpdateResponseDto res = authService.updateMyInfo(1, req);

            // then
            assertThat(res.getName()).isEqualTo("홍길동");
            assertThat(res.getEmail()).isEqualTo("hong@lgup.com");
            assertThat(res.getBirth()).isNull();
            assertThat(res.getPhone()).isNull();
        }

        @Test
        @DisplayName("실패 - 요청 본문 null")
        void fail_nullRequest() {
            assertThatThrownBy(() -> authService.updateMyInfo(1, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 이름 blank")
        void fail_blankName() {
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("   ")
                    .email("hong@lgup.com")
                    .build();

            assertThatThrownBy(() -> authService.updateMyInfo(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 이메일 blank")
        void fail_blankEmail() {
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("")
                    .build();

            assertThatThrownBy(() -> authService.updateMyInfo(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }

        @Test
        @DisplayName("실패 - 직원 존재하지 않음")
        void fail_employeeNotFound() {
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .build();

            given(employeeRepository.findById(999)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.updateMyInfo(999, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("실패 - 이메일 중복 (본인 제외 다른 직원)")
        void fail_emailDuplicate() {
            MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("taken@lgup.com")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("taken@lgup.com", 1)).willReturn(true);

            assertThatThrownBy(() -> authService.updateMyInfo(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.DUPLICATE_EMAIL));
        }

        @Test
        @DisplayName("실패 - 생년월일 형식 오류")
        void fail_invalidBirthFormat() {
        	MyInfoUpdateRequestDto req = MyInfoUpdateRequestDto.builder()
                    .name("홍길동")
                    .email("hong@lgup.com")
                    .birth("1990/05/15")
                    .build();

            given(employeeRepository.findById(1)).willReturn(Optional.of(mockEmployee));
            given(employeeRepository.existsByEmailAndEmpIdNot("hong@lgup.com", 1)).willReturn(false);

            assertThatThrownBy(() -> authService.updateMyInfo(1, req))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_INPUT));
        }
    }
}
