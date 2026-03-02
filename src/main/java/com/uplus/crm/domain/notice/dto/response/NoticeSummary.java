package com.uplus.crm.domain.notice.dto.response;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record NoticeSummary(
        int noticeId,
        String title,
        NoticeType noticeType,
        NoticeStatus status,
        boolean isPinned,
        boolean isNew,
        String authorName,
        int viewCount,
        LocalDateTime createdAt
) {
    public static NoticeSummary from(Notice notice) {
        boolean isNew = notice.getCreatedAt() != null
                && ChronoUnit.DAYS.between(notice.getCreatedAt(), LocalDateTime.now()) < 3;

        return new NoticeSummary(
                notice.getNoticeId() != null ? notice.getNoticeId() : 0,
                notice.getTitle(),
                notice.getNoticeType(),
                notice.getStatus(),
                notice.isPinned(),
                isNew,
                notice.getEmployee() != null ? notice.getEmployee().getName() : null,
                notice.getViewCount(),
                notice.getCreatedAt()
        );
    }
}
