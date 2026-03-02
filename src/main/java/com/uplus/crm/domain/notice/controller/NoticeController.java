package com.uplus.crm.domain.notice.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.request.NoticeUpdateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeSummary;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notice", description = "공지사항 CRUD API")
@RestController
@RequestMapping("/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ── POST /v1/notices ──────────────────────────────────────────────────────

    @Operation(summary = "공지 등록 (ADMIN만)",
               description = "sendNotification=true 이면 대상 역할 직원에게 알림 발송")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NoticeResponse> createNotice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        NoticeResponse response = noticeService.createNotice(request, userDetails.getEmpId());
        return ApiResponse.ok("공지사항이 등록되었습니다.", response);
    }

    // ── GET /v1/notices ───────────────────────────────────────────────────────

    @Operation(summary = "공지 목록 조회 (역할 기반)",
               description = "ADMIN: DELETED 제외 전체 / AGENT: ACTIVE + visible 기간 + 역할 필터")
    @GetMapping
    public ApiResponse<Page<NoticeSummary>> getNoticeList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<NoticeSummary> page = noticeService.getNoticeList(
                userDetails.getEmpId(), userDetails.getRoleName(), pageable);
        return ApiResponse.ok(page);
    }

    // ── GET /v1/notices/{noticeId} ────────────────────────────────────────────

    @Operation(summary = "공지 상세 조회",
               description = "조회수 증가 + 읽음 이력 저장 + 해당 공지 알림 읽음 처리")
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeResponse> getNoticeDetail(
            @PathVariable int noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(noticeService.getNoticeDetail(noticeId, userDetails.getEmpId(), userDetails.getRoleName()));
    }

    // ── PUT /v1/notices/{noticeId} ────────────────────────────────────────────

    @Operation(summary = "공지 수정 (ADMIN만)",
               description = "visibleFrom 변경 시 status 자동 재결정. DELETED 상태는 수정 불가")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{noticeId}")
    public ApiResponse<NoticeResponse> updateNotice(
            @PathVariable int noticeId,
            @Valid @RequestBody NoticeUpdateRequest request
    ) {
        return ApiResponse.ok(noticeService.updateNotice(noticeId, request));
    }

    // ── DELETE /v1/notices/{noticeId} ─────────────────────────────────────────

    @Operation(summary = "공지 삭제 (ADMIN만)", description = "소프트 삭제 — status → DELETED")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> deleteNotice(
            @PathVariable int noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        noticeService.deleteNotice(noticeId, userDetails.getEmpId());
        return ApiResponse.ok("공지사항이 삭제되었습니다.", null);
    }
}
