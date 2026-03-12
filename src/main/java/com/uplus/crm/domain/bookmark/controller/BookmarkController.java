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
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.service.BookmarkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bookmark", description = "북마크 API")
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * [운영정책 북마크 등록]
     * POST /bookmarks/manuals/{manualId}
     */
    @Operation(
        summary = "매뉴얼 북마크 추가",
        description = "특정 운영정책 매뉴얼을 북마크에 추가합니다."
    )
    @PostMapping("/manuals/{manualId}")
    public ApiResponse<BookmarkToggleResponseDto> addManualBookmark(
            @Parameter(description = "북마크할 매뉴얼 ID", example = "1")
            @PathVariable("manualId") Integer manualId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.addManualBookmark(empId, manualId));
    }

    /**
     * [운영정책 북마크 해제]
     * DELETE /bookmarks/manuals/{manualId}
     */
    @Operation(
        summary = "매뉴얼 북마크 삭제",
        description = "특정 운영정책 매뉴얼 북마크를 해제합니다."
    )
    @DeleteMapping("/manuals/{manualId}")
    public ApiResponse<BookmarkToggleResponseDto> removeManualBookmark(
            @Parameter(description = "북마크 해제할 매뉴얼 ID", example = "1")
            @PathVariable("manualId") Integer manualId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.removeManualBookmark(empId, manualId));
    }

    /**
     * [내 운영정책 북마크 목록 조회]
     * GET /bookmarks/manuals
     */
    @Operation(
        summary = "매뉴얼 북마크 목록 조회",
        description = "로그인한 사용자의 운영정책 매뉴얼 북마크 목록을 조회합니다."
    )
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
    @Operation(
        summary = "상담요약 북마크 추가",
        description = "특정 상담요약 결과를 북마크에 추가합니다."
    )
    @PostMapping("/consultations/{consultId}")
    public ApiResponse<BookmarkToggleResponseDto> addConsultationBookmark(
            @Parameter(description = "북마크할 상담요약 ID", example = "1")
            @PathVariable("consultId") Long consultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.addConsultationBookmark(empId, consultId));
    }

    /**
     * [상담요약 북마크 해제]
     * DELETE /bookmarks/consultations/{consultId}
     */
    @Operation(
        summary = "상담요약 북마크 삭제",
        description = "특정 상담요약 결과 북마크를 해제합니다."
    )
    @DeleteMapping("/consultations/{consultId}")
    public ApiResponse<BookmarkToggleResponseDto> removeConsultationBookmark(
            @Parameter(description = "북마크 해제할 상담요약 ID", example = "1")
            @PathVariable("consultId") Long consultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.removeConsultationBookmark(empId, consultId));
    }

    /**
     * [내 상담요약 북마크 목록 조회]
     * GET /bookmarks/consultations
     */
    @Operation(
        summary = "상담요약 북마크 목록 조회",
        description = "로그인한 사용자의 상담요약 북마크 목록을 조회합니다."
    )
    @GetMapping("/consultations")
    public ApiResponse<List<ConsultationBookmarkResponseDto>> getConsultationBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.getConsultationBookmarks(empId));
    }
    
    /**
     * [매뉴얼 북마크 상세 조회]
     * GET /bookmarks/manuals/{manualId}/detail
     */
    @Operation(
        summary = "매뉴얼 북마크 상세 조회",
        description = "로그인한 사용자가 북마크한 특정 운영정책 매뉴얼의 상세 정보를 조회합니다."
    )
    @GetMapping("/manuals/{manualId}/detail")
    public ApiResponse<ManualBookmarkDetailResponseDto> getManualBookmarkDetail(
            @Parameter(description = "조회할 매뉴얼 ID", example = "1")
            @PathVariable("manualId") Integer manualId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.getManualBookmarkDetail(empId, manualId));
    }


    /**
     * [상담요약 북마크 상세 조회]
     * GET /bookmarks/consultations/{consultId}/detail
     */
    @Operation(
        summary = "상담요약 북마크 상세 조회",
        description = "로그인한 사용자가 북마크한 특정 상담요약 결과의 상세 정보를 조회합니다."
    )
    @GetMapping("/consultations/{consultId}/detail")
    public ApiResponse<ConsultationBookmarkDetailResponseDto> getConsultationBookmarkDetail(
            @Parameter(description = "조회할 상담요약 ID", example = "1")
            @PathVariable("consultId") Long consultId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        int empId = userDetails.getEmpId();
        return ApiResponse.ok(bookmarkService.getConsultationBookmarkDetail(empId, consultId));
    }
}