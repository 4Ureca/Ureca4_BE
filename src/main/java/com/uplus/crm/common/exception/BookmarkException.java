package com.uplus.crm.common.exception;

import lombok.Getter;

@Getter
public class BookmarkException extends RuntimeException {

    private final BookmarkErrorCode errorCode;

    public BookmarkException(BookmarkErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BookmarkException(BookmarkErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}