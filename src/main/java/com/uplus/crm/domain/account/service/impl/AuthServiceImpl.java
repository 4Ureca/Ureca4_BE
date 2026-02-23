package com.uplus.crm.domain.account.service.impl;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.RefreshToken;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.RefreshTokenRepository;
import com.uplus.crm.domain.account.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────
    // POST /auth/google
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response) {

        // 1. Google로부터 이메일 추출 (Google API 연동 로직 추가 필요)
        String email = extractEmailFromGoogle(request.getAuthorizationCode(), request.getRedirectUri());

        // 2. 이메일로 직원 조회
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("연동된 계정이 없습니다."));

        // 3. 토큰 발급
        String accessToken = generateAccessToken(employee);
        String newRefreshToken = generateRefreshToken(employee);
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(1);

        // 4. Refresh Token 저장
        saveRefreshToken(employee, newRefreshToken, expiredAt.plusDays(7));

        // 5. HttpOnly Cookie에 Refresh Token 세팅
        setRefreshTokenCookie(response, newRefreshToken);

        return GoogleAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiredAt(expiredAt)
                .isNewUser(false)
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /auth/login
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {

        // 1. 직원 조회
        Employee employee = employeeRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. 토큰 발급
        String accessToken = generateAccessToken(employee);
        String newRefreshToken = generateRefreshToken(employee);
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(1);

        // 4. 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByEmployee_EmpId(employee.getEmpId());
        saveRefreshToken(employee, newRefreshToken, expiredAt.plusDays(7));

        // 5. HttpOnly Cookie에 Refresh Token 세팅
        setRefreshTokenCookie(response, newRefreshToken);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiredAt(expiredAt)
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /auth/logout
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response) {

        // 1. Cookie에서 Refresh Token 추출
        String refreshToken = extractRefreshTokenFromCookie(request);

        // 2. DB에서 Refresh Token 삭제
        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        // 3. Cookie 만료 처리
        clearRefreshTokenCookie(response);

        return LogoutResponseDto.builder()
                .message("로그아웃 되었습니다.")
                .build();
    }

    // ─────────────────────────────────────────────
    // POST /auth/refresh
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public TokenRefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {

        // 1. Cookie에서 Refresh Token 추출
        String oldRefreshToken = extractRefreshTokenFromCookie(request);

        // 2. DB에서 Refresh Token 조회 및 만료 검증
        RefreshToken tokenEntity = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));

        if (tokenEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh Token이 만료되었습니다.");
        }

        // 3. 새 토큰 발급 (Refresh Token Rotation)
        Employee employee = tokenEntity.getEmployee();
        String newAccessToken = generateAccessToken(employee);
        String newRefreshToken = generateRefreshToken(employee);
        LocalDateTime expiredAt = LocalDateTime.now().plusHours(1);

        // 4. 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.delete(tokenEntity);
        saveRefreshToken(employee, newRefreshToken, expiredAt.plusDays(7));

        // 5. HttpOnly Cookie 갱신
        setRefreshTokenCookie(response, newRefreshToken);

        return TokenRefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiredAt(expiredAt)
                .build();
    }

    // ─────────────────────────────────────────────
    // PUT /auth/me/password
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public PasswordChangeResponseDto changePassword(Integer empId, PasswordChangeRequestDto request) {

        // 1. 직원 조회
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("직원 정보를 찾을 수 없습니다."));

        // 2. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호 확인 일치 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 4. 비밀번호 변경 (Entity에 updatePassword 메서드 추가 필요)
        employee.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        return PasswordChangeResponseDto.builder()
                .message("비밀번호가 변경되었습니다.")
                .build();
    }

    // ─────────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────────

    private String extractEmailFromGoogle(String authorizationCode, String redirectUri) {
        // TODO: Google OAuth API 연동 로직 구현 필요
        throw new UnsupportedOperationException("Google OAuth 연동 구현 필요");
    }

    private String generateAccessToken(Employee employee) {
        // TODO: JWT 생성 로직 구현 필요
        return "access_token_" + employee.getEmpId();
    }

    private String generateRefreshToken(Employee employee) {
        // TODO: JWT or UUID 기반 Refresh Token 생성 로직 구현 필요
        return "refresh_token_" + employee.getEmpId();
    }

    private void saveRefreshToken(Employee employee, String token, LocalDateTime expiredAt) {
        refreshTokenRepository.save(RefreshToken.builder()
                .employee(employee)
                .refreshToken(token)
                .expiredAt(expiredAt)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new RuntimeException("Refresh Token이 없습니다.");
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token이 없습니다."));
    }
}