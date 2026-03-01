package com.uplus.crm.domain.notice.dto.request;

import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record NoticeCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        String content,

        NoticeType noticeType,       // null 이면 GENERAL 기본값 적용
        TargetRole targetRole,       // null 이면 ALL 기본값 적용
        boolean isPinned,
        boolean sendNotification,    // UI의 nf_notify_toggle
        LocalDateTime visibleFrom,   // null 이면 즉시 게시
        LocalDateTime visibleTo
) {}
