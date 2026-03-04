package com.uplus.crm.common.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 인증 성공 후 SecurityContext에 저장되는 인증 주체.
 * {@code @AuthenticationPrincipal CustomUserDetails} 로 Controller에서 취득.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails {

    private final int empId;

    /** job_roles.role_name 값 — "관리자" | "상담사" */
    private final String roleName;

    public boolean isAdmin() {
        return "관리자".equals(roleName);
    }
}
