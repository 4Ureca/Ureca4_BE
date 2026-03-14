package com.uplus.crm.common.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.uplus.crm.domain.bookmark")
public class BookmarkExceptionHandler {

    @ExceptionHandler(BookmarkException.class)
    public ResponseEntity<ErrorResponse> handleBookmarkException(BookmarkException ex) {
        BookmarkErrorCode errorCode = ex.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .error(errorCode.name())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }
}