package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // POST /auth/google — Google OAuth 연동
    GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response);

    // POST /auth/login — 일반 로그인
    LoginResponseDto login(LoginRequestDto request, HttpServletResponse response);

    // POST /auth/logout — 로그아웃 (HttpOnly Cookie)
    LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response);

    // POST /auth/refresh — 토큰 갱신 (HttpOnly Cookie)
    TokenRefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response);

    // PUT /auth/me/password — 비밀번호 변경
    PasswordChangeResponseDto changePassword(Integer empId, PasswordChangeRequestDto request);
}