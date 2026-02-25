package com.uplus.crm.domain.account.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.uplus.crm.domain.account.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
/*
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
*/
    
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

    // ✅ 추가: PUT /auth/me (내 정보 수정)
    @Override
    @Transactional
    public MyInfoUpdateResponseDto updateMyInfo(Integer empId, MyInfoUpdateRequestDto req) {

        // 파라미터 최소 검증 (원하면 더 강화 가능)
        if (req == null || isBlank(req.getName()) || isBlank(req.getEmail())) {
            // 너희 ErrorCode에 "파라미터 오류"가 뭐가 있는지 몰라서 일단 INVALID_TOKEN 말고,
            // 일반적으로 쓰는 INVALID_CREDENTIALS를 쓰면 의미가 안 맞음.
            // 여기서는 EMPLOYEE_NOT_FOUND 같은 걸 쓰면 더 이상하고…
            // => 가능하면 ErrorCode에 INVALID_PARAMETER 같은 게 있으면 그걸로 바꿔줘.
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 이메일 중복(본인 제외)
        if (employeeRepository.existsByEmailAndEmpIdNot(req.getEmail(), empId)) {
            // ErrorCode에 이메일 중복 코드가 있으면 그걸로 바꾸는게 제일 좋음
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        LocalDate birth = parseLocalDateOrNull(req.getBirth());

        employee.updateAccountInfo(
                req.getName(),
                req.getEmail(),
                req.getPhone(),
                birth,
                req.getGender()
        );

        return MyInfoUpdateResponseDto.builder()
                .empId(employee.getEmpId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .birth(employee.getBirth() == null ? null : employee.getBirth().toString())
                .gender(employee.getGender())
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

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value); // yyyy-MM-dd
        } catch (Exception e) {
            // 날짜 형식 에러도 ErrorCode가 있으면 그걸로 교체 추천
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

	@Override
	public GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}
}