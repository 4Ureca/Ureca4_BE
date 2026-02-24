package com.uplus.crm.domain.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.account.dto.response.EmailCheckResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoResponseDto;
import com.uplus.crm.domain.account.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 및 계정 정보 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "구글 이메일 중복 확인", description = "시스템에 등록된 이메일인지 확인하여 가입 가능 여부를 반환합니다.")
    @GetMapping("/google/email-check")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.checkEmailAvailability(email));
    }

    @Operation(summary = "로그인한 계정 정보 조회", description = "현재 로그인된 직원의 상세 정보와 부서/개별 권한이 합산된 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponseDto> getMyInfo() {
        // [테스트용] 실제 구현 시에는 SecurityContext에서 현재 empId를 추출해야 합니다.
        // 예: Integer currentEmpId = SecurityUtil.getCurrentMemberId();
        Integer currentEmpId = 1; 
        
        return ResponseEntity.ok(authService.getMyInfo(currentEmpId));
    }
    /*
    //JWT작업완료시 위에꺼 지우고 아래꺼 사용 CustomUserDetails -> JWT를 통해 인증된 사용자의 정보가 core 패키지의 UserDetails에 담겨들어올 예정
    @Operation(summary = "로그인한 계정 정보 조회", description = "현재 로그인된 직원의 상세 정보와 부서/개별 권한이 합산된 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 토큰에서 이미 인증된 사용자의 ID를 바로 꺼내 씁니다.
        Integer currentEmpId = userDetails.getEmpId(); 
        return ResponseEntity.ok(authService.getMyInfo(currentEmpId));
    }*/
    
}