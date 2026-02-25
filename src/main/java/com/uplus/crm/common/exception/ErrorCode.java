package com.uplus.crm.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),

    // 401 Unauthorized
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

    // 404 Not Found
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원 정보를 찾을 수 없습니다."),
    ACCOUNT_NOT_LINKED(HttpStatus.NOT_FOUND, "연동된 계정이 없습니다."),

    // 500 Internal Server Error
    GOOGLE_AUTH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Google OAuth 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
