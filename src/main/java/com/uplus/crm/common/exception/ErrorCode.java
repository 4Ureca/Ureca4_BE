package com.uplus.crm.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    INVALID_PERMISSION_IDS(HttpStatus.BAD_REQUEST, "유효하지 않은 권한 ID가 포함되어 있습니다."),
    MISSING_TARGET_EMPID(HttpStatus.BAD_REQUEST, "조회할 상담사 ID가 누락되었습니다."), // 관리자용
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "지원하지 않는 조회 주기입니다. (daily/weekly/monthly)"),

    // 401 Unauthorized
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

    // 403 Forbidden
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),

    // 404 Not Found
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "직원 정보를 찾을 수 없습니다."),
    ACCOUNT_NOT_LINKED(HttpStatus.NOT_FOUND, "연동된 계정이 없습니다."),
    FILTER_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "필터 그룹을 찾을 수 없습니다."),
    FILTER_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 필터입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    NOTICE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 공지사항입니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),
    CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "상담 데이터가 없습니다."),

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부서입니다."),
    JOB_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 직무입니다."),

    CONSULTATION_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "상담 결과서를 찾을 수 없습니다."),
    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "요약 데이터를 찾을 수 없습니다."),
    
    MANUAL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 매뉴얼입니다."),
    CATEGORY_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리 정책입니다."),

    AGENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상담사입니다."),
    REPORT_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 날짜에 해당하는 리포트 데이터가 없습니다."),

  // 409 Conflict
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 로그인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),


    // 500 Internal Server Error
    DATA_INTEGRITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 무결성 오류가 발생했습니다."),
    GOOGLE_AUTH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Google OAuth 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
