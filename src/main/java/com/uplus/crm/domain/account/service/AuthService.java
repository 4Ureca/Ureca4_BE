package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    GoogleAuthResponseDto googleLogin(GoogleAuthRequestDto request, HttpServletResponse response);
    LoginResponseDto login(LoginRequestDto request, HttpServletResponse response);
    LogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response);
    TokenRefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response);
    PasswordChangeResponseDto changePassword(Integer empId, PasswordChangeRequestDto request);
    EmailCheckResponseDto checkEmailAvailability(String email);
    MyInfoResponseDto getMyInfo(Integer empId);
}