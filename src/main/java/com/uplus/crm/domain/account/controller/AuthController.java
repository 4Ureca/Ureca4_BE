package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.GoogleAuthRequestDto;
import com.uplus.crm.domain.account.dto.request.LoginRequestDto;
import com.uplus.crm.domain.account.dto.request.PasswordChangeRequestDto;
import com.uplus.crm.domain.account.dto.response.*;
import com.uplus.crm.domain.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 및 계정 정보 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // --- 구글 관련 인증 ---

    @Operation(summary = "Google OAuth 로그인", description = "Google authorization code를 전달받아 로그인 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Google 로그인 성공"),
            @ApiResponse(responseCode = "400", description = "authorization code 유효하지 않음"),
            @ApiResponse(responseCode = "401", description = "Google 인증 실패"),
            @ApiResponse(responseCode = "404", description = "연동된 계정 없음")
    })
    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDto> googleLogin(
            @Valid @RequestBody GoogleAuthRequestDto request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.googleLogin(request, response));
    }

    @Operation(summary = "구글 이메일 중복 확인", description = "시스템에 등록된 이메일인지 확인하여 가입 가능 여부를 반환합니다.")
    @GetMapping("/google/email-check")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.checkEmailAvailability(email));
    }

    // --- 일반 인증 및 토큰 관리 ---

    @Operation(summary = "일반 로그인", description = "계정 아이디와 비밀번호로 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(summary = "로그아웃", description = "HttpOnly Cookie의 Refresh Token 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token 유효하지 않음")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    @Operation(summary = "토큰 갱신", description = "HttpOnly Cookie의 Refresh Token으로 Access Token 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 만료 또는 유효하지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(request, response));
    }

    // --- 계정 정보 조회 및 수정 ---
    @Operation(summary = "로그인한 계정 정보 조회", description = "현재 로그인된 직원의 상세 정보와 권한 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponseDto> getMyInfo(@AuthenticationPrincipal Integer empId) {
        return ResponseEntity.ok(authService.getMyInfo(empId));
    }
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "새 비밀번호와 확인 비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "현재 비밀번호 불일치")
    })
    @PutMapping("/me/password")
    public ResponseEntity<PasswordChangeResponseDto> changePassword(
            @AuthenticationPrincipal Integer empId,
            @Valid @RequestBody PasswordChangeRequestDto request) {
        return ResponseEntity.ok(authService.changePassword(empId, request));
    }
}