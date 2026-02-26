package com.uplus.crm.domain.account.service.impl;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.common.util.CookieUtil;
import com.uplus.crm.common.util.GoogleOAuthUtil;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.*;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.RefreshToken;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.MenuRepository; // 추가
import com.uplus.crm.domain.account.repository.mysql.RefreshTokenRepository;
import com.uplus.crm.domain.account.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MenuRepository menuRepository; // 💡 주입 추가
    private final PasswordEncoder passwordEncoder;
    private final GoogleOAuthUtil googleOAuthUtil;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    public EmailCheckResponseDto checkEmailAvailability(String email) {
        boolean isDuplicate = employeeRepository.existsByEmail(email);
        return EmailCheckResponseDto.builder().available(!isDuplicate).email(email).build();
    }

    // --- 2. 로그인한 계정 정보 조회 (메뉴 리스트 포함) ---
    @Override
    public MyInfoResponseDto getMyInfo(Integer empId) {
        Employee employee = employeeRepository.findByIdWithDetails(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeDetail detail = employee.getEmployeeDetail();
        List<String> menuCodes = new ArrayList<>();

        if (detail != null && detail.getJobRole() != null) {
            menuCodes = menuRepository.findMenuCodesByJobRoleId(detail.getJobRole().getJobRoleId());
        }
        
        return convertToMyInfoDto(employee, menuCodes);
    }

    @Override
    @Transactional
    public GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response) {
        String email = googleOAuthUtil.getEmailFromAuthCode(request.getAuthorizationCode(), request.getRedirectUri());
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
        return LogoutResponseDto.builder().message("로그아웃 되었습니다.").build();
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
        return PasswordChangeResponseDto.builder().message("비밀번호가 변경되었습니다.").build();
    }

    // ─────────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────────

    private MyInfoResponseDto convertToMyInfoDto(Employee e, List<String> menuCodes) {
        EmployeeDetail d = e.getEmployeeDetail();
        return MyInfoResponseDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .birth(e.getBirth() != null ? e.getBirth().toString() : null)
                .gender(e.getGender())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .deptId(d != null && d.getDepartment() != null ? d.getDepartment().getDeptId() : null)
                .deptName(d != null && d.getDepartment() != null ? d.getDepartment().getDeptName() : null)
                .jobRoleId(d != null && d.getJobRole() != null ? d.getJobRole().getJobRoleId() : null)
                .roleName(d != null && d.getJobRole() != null ? d.getJobRole().getRoleName() : null)
                .joinedAt(d != null && d.getJoinedAt() != null ? d.getJoinedAt().toString() : null)
                .menuCodes(menuCodes) // 💡 리스트 설정
                .build();
    }

    private GoogleAuthResponseDto issueTokensAndRespond(Employee employee, HttpServletResponse response, boolean deleteExisting) {
        if (deleteExisting) {
            refreshTokenRepository.deleteByEmployee_EmpId(employee.getEmpId());
        }

        String accessToken = jwtUtil.generateAccessToken(employee.getEmpId(), employee.getLoginId());
        String refreshToken = jwtUtil.generateRefreshToken(employee.getEmpId());
        
        refreshTokenRepository.save(RefreshToken.builder()
                .employee(employee)
                .refreshToken(refreshToken)
                .expiredAt(jwtUtil.getRefreshTokenExpiredAt())
                .createdAt(LocalDateTime.now())
                .build());

        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        return GoogleAuthResponseDto.builder()
                .accessToken(accessToken)
                .expiredAt(jwtUtil.getAccessTokenExpiredAt())
                .isNewUser(false)
                .build();
    }

    private String extractRefreshTokenOrThrow(HttpServletRequest request) {
        return cookieUtil.extractRefreshToken(request)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_TOKEN));
    }
}