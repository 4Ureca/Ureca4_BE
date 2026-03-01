package com.uplus.crm.domain.notice.dto.response;

import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeResponse {
    private Integer noticeId;
    private String title;
    private String content;
    private Integer empId;
    private Boolean isPinned;
    private Integer viewCount;
    private NoticeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime visibleFrom;
    private LocalDateTime visibleTo;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .empId(notice.getEmployee().getEmpId())
                .isPinned(notice.getIsPinned())
                .viewCount(notice.getViewCount())
                .status(notice.getStatus())
                .createdAt(notice.getCreatedAt())
                .visibleFrom(notice.getVisibleFrom())
                .visibleTo(notice.getVisibleTo())
                .build();
    }
}
