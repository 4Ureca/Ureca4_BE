package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.GoogleAuthResponseDto;
import com.uplus.crm.domain.account.dto.response.LoginResponseDto;
import com.uplus.crm.domain.account.dto.response.LogoutResponseDto;
import com.uplus.crm.domain.account.dto.response.PasswordChangeResponseDto;
import com.uplus.crm.domain.account.dto.response.TokenRefreshResponseDto;
import com.uplus.crm.domain.account.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /auth/google
    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDto> googleLogin(
            @RequestBody GoogleAuthRequestDto request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.googleLogin(request, response));
    }

    // POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.login(request, response));
    }

    // POST /auth/logout
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.logout(request, response));
    }

    // POST /auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.refresh(request, response));
    }

    // PUT /auth/me/password
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponseDto> changePassword(
            @AuthenticationPrincipal Integer empId,
            @RequestBody PasswordChangeRequestDto request) {

        return ResponseEntity.ok(authService.changePassword(empId, request));
    }
}