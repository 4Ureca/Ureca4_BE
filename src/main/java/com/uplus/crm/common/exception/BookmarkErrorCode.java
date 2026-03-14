package com.uplus.crm.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkErrorCode {

    MANUAL_BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 운영정책 북마크입니다."),
    MANUAL_BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "운영정책 북마크가 존재하지 않습니다."),
    CONSULTATION_NOT_FOUND(HttpStatus.NOT_FOUND, "상담요약 데이터가 존재하지 않습니다."),
    CONSULTATION_BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 상담요약 북마크입니다."),
    CONSULTATION_BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "상담요약 북마크가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
