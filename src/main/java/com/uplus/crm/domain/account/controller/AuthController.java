package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.response.EmailCheckResponseDto;
import com.uplus.crm.domain.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth - Google Login", description = "인증 및 구글 로그인 관련 API")
@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "구글 이메일 중복 확인", description = "시스템에 등록된 이메일인지 확인하여 가입 가능 여부를 반환합니다.")
    @GetMapping("/email-check")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.checkEmailAvailability(email));
    }
}