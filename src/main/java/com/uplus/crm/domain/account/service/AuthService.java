package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // --- 기존 develop 기능 (인증/토큰) ---
    GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response);
    LoginResponseDto login(LoginRequestDto request, HttpServletResponse response);
    LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response);
    TokenRefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response);
    PasswordChangeResponseDto changePassword(Integer empId, PasswordChangeRequestDto request);

    // --- 승혁 님 추가 기능 (중복체크/내정보) ---
    EmailCheckResponseDto checkEmailAvailability(String email);
    MyInfoResponseDto getMyInfo(Integer empId);
}