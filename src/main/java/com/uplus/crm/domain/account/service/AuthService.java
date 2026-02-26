package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.MyInfoUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.EmailCheckResponseDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;

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
    
    MyInfoUpdateResponseDto updateMyInfo(Integer empId, MyInfoUpdateRequestDto req);
}