package com.uplus.crm.domain.account.service.impl;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.CookieUtil;
import com.uplus.crm.common.util.GoogleOAuthUtil;
import com.uplus.crm.common.util.JwtUtil;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuthUtil googleOAuthUtil;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    @Transactional
    public GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response) {
        String email = googleOAuthUtil.getEmailFromAuthCode(
                request.getAuthorizationCode(),
                request.getRedirectUri()
        );

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_LINKED));

        return issueTokensAndRespond(employee, response, true);
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {
        Employee employee = employeeRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        GoogleAuthResponseDto tokenResponse = issueTokensAndRespond(employee, response, true);

        return LoginResponseDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .expiredAt(tokenResponse.getExpiredAt())
                .build();
    }

    @Override
    @Transactional
    public LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenOrThrow(request);

        refreshTokenRepository.deleteByRefreshToken(refreshToken);
        cookieUtil.clearRefreshTokenCookie(response);

        return LogoutResponseDto.builder()
                .message("로그아웃 되었습니다.")
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = extractRefreshTokenOrThrow(request);

        RefreshToken tokenEntity = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (tokenEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        Employee employee = tokenEntity.getEmployee();
        refreshTokenRepository.delete(tokenEntity);

        GoogleAuthResponseDto tokenResponse = issueTokensAndRespond(employee, response, false);

        return TokenRefreshResponseDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .expiredAt(tokenResponse.getExpiredAt())
                .build();
    }

    @Override
    @Transactional
    public PasswordChangeResponseDto changePassword(Integer empId, PasswordChangeRequestDto request) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        employee.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        return PasswordChangeResponseDto.builder()
                .message("비밀번호가 변경되었습니다.")
                .build();
    }

    // ─────────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────────

    private GoogleAuthResponseDto issueTokensAndRespond(Employee employee,
                                                        HttpServletResponse response,
                                                        boolean deleteExisting) {
        if (deleteExisting) {
            refreshTokenRepository.deleteByEmployee_EmpId(employee.getEmpId());
        }

        String accessToken = jwtUtil.generateAccessToken(employee.getEmpId(), employee.getLoginId());
        String refreshToken = jwtUtil.generateRefreshToken(employee.getEmpId());
        LocalDateTime accessExpiredAt = jwtUtil.getAccessTokenExpiredAt();
        LocalDateTime refreshExpiredAt = jwtUtil.getRefreshTokenExpiredAt();

        refreshTokenRepository.save(RefreshToken.builder()
                .employee(employee)
                .refreshToken(refreshToken)
                .expiredAt(refreshExpiredAt)
                .createdAt(LocalDateTime.now())
                .build());

        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        return GoogleAuthResponseDto.builder()
                .accessToken(accessToken)
                .expiredAt(accessExpiredAt)
                .isNewUser(false)
                .build();
    }

    private String extractRefreshTokenOrThrow(HttpServletRequest request) {
        return cookieUtil.extractRefreshToken(request)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_TOKEN));
    }
}
