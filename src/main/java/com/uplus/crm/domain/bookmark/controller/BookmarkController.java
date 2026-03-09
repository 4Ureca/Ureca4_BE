package com.uplus.crm.domain.bookmark.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.bookmark.dto.BookmarkToggleResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.service.BookmarkService;

import lombok.RequiredArgsConstructor;

/**
 * 북마크 관련 API Controller
 *
 * - SecurityConfig에서 /bookmarks/** 는 permitAll이 아니므로 "로그인(JWT 인증) 필수"
 * - JwtAuthFilter가 토큰 검증 성공 시 SecurityContext에 CustomUserDetails(empId, roleName)를 넣어줌
 * - 그래서 Controller에서는 @AuthenticationPrincipal CustomUserDetails 로 empId를 바로 받을 수 있음
 *
 * 구현 범위(현재):
 * - 운영정책(manual) 북마크 등록/해제/목록
 * - 상담요약(consultation result) 북마크 등록/해제/목록
 * - 우수사례(best_practice)는 아직 제외
 */
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * [운영정책 북마크 등록]
     * POST /bookmarks/manuals/{manualId}
     */
    @PostMapping("/manuals/{manualId}")
    public ApiResponse<BookmarkToggleResponseDto> addManualBookmark(
            @PathVariable("manualId") Integer manualId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.addManualBookmark(empId, manualId));
    }

    /**
     * [운영정책 북마크 해제]
     * DELETE /bookmarks/manuals/{manualId}
     */
    @DeleteMapping("/manuals/{manualId}")
    public ApiResponse<BookmarkToggleResponseDto> removeManualBookmark(
            @PathVariable("manualId") Integer manualId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.removeManualBookmark(empId, manualId));
    }

    /**
     * [내 운영정책 북마크 목록 조회]
     * GET /bookmarks/manuals
     */
    @GetMapping("/manuals")
    public ApiResponse<List<ManualBookmarkResponseDto>> getManualBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.getManualBookmarks(empId));
    }

    /**
     * [상담요약 북마크 등록]
     * POST /bookmarks/consultations/{consultId}
     */
    @PostMapping("/consultations/{consultId}")
    public ApiResponse<BookmarkToggleResponseDto> addConsultationBookmark(
            @PathVariable("consultId") Long consultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.addConsultationBookmark(empId, consultId));
    }

    /**
     * [상담요약 북마크 해제]
     * DELETE /bookmarks/consultations/{consultId}
     */
    @DeleteMapping("/consultations/{consultId}")
    public ApiResponse<BookmarkToggleResponseDto> removeConsultationBookmark(
            @PathVariable("consultId") Long consultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.removeConsultationBookmark(empId, consultId));
    }

    /**
     * [내 상담요약 북마크 목록 조회]
     * GET /bookmarks/consultations
     */
    @GetMapping("/consultations")
    public ApiResponse<List<ConsultationBookmarkResponseDto>> getConsultationBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.getConsultationBookmarks(empId));
    }
}