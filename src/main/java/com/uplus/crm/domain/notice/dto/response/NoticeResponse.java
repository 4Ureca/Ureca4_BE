package com.uplus.crm.domain.notice.dto.response;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record NoticeResponse(
        int noticeId,
        String title,
        String content,
        String authorName,
        NoticeType noticeType,
        TargetRole targetRole,
        NoticeStatus status,
        boolean isPinned,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime visibleFrom,
        LocalDateTime visibleTo,
        boolean isNew           // createdAt 기준 3일 이내이면 true
) {
    public static NoticeResponse from(Notice notice) {
        boolean isNew = notice.getCreatedAt() != null
                && ChronoUnit.DAYS.between(notice.getCreatedAt(), LocalDateTime.now()) < 3;

        return new NoticeResponse(
                notice.getNoticeId() != null ? notice.getNoticeId() : 0,
                notice.getTitle(),
                notice.getContent(),
                notice.getEmployee() != null ? notice.getEmployee().getName() : null,
                notice.getNoticeType(),
                notice.getTargetRole(),
                notice.getStatus(),
                notice.isPinned(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getVisibleFrom(),
                notice.getVisibleTo(),
                isNew
        );
    }
}
