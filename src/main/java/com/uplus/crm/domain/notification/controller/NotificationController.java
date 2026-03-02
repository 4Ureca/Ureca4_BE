package com.uplus.crm.domain.notification.controller;

import com.uplus.crm.common.response.ApiResponse;
import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.domain.notification.dto.NotificationResponse;
import com.uplus.crm.domain.notification.dto.NotificationSettingsResponse;
import com.uplus.crm.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── GET /v1/notifications ─────────────────────────────────────────────────

    @Operation(summary = "알림 목록 조회", description = "최신순 페이징")
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(
                notificationService.getNotifications(userDetails.getEmpId(), pageable));
    }

    // ── GET /v1/notifications/unread-count ────────────────────────────────────

    @Operation(summary = "미읽음 알림 수 조회")
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.getUnreadCount(userDetails.getEmpId()));
    }

    // ── PATCH /v1/notifications/{notificationId}/read ─────────────────────────

    @Operation(summary = "단건 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> readNotification(
            @PathVariable long notificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.readNotification(notificationId, userDetails.getEmpId());
        return ApiResponse.ok(null);
    }

    // ── PATCH /v1/notifications/read-all ─────────────────────────────────────

    @Operation(summary = "전체 읽음 처리")
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAllAsRead(userDetails.getEmpId());
        return ApiResponse.ok(null);
    }

    // ── GET /v1/notifications/settings ───────────────────────────────────────

    @Operation(summary = "알림 수신 설정 조회")
    @GetMapping("/settings")
    public ApiResponse<NotificationSettingsResponse> getSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(notificationService.getSettings(userDetails.getEmpId()));
    }

    // ── PATCH /v1/notifications/settings/{field} ──────────────────────────────

    @Operation(summary = "알림 수신 설정 토글",
               description = "field: notice | best_practice | policy_change")
    @PatchMapping("/settings/{field}")
    public ApiResponse<NotificationSettingsResponse> toggleSetting(
            @PathVariable String field,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(
                notificationService.toggleSetting(userDetails.getEmpId(), field));
    }
}
