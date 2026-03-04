package com.uplus.crm.domain.notice.entity;

import com.uplus.crm.domain.account.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Integer noticeId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "notice_type", nullable = false, length = 20)
    @Builder.Default
    private NoticeType noticeType = NoticeType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role", length = 10)
    @Builder.Default
    private TargetRole targetRole = TargetRole.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NoticeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "visible_from")
    private LocalDateTime visibleFrom;

    @Column(name = "visible_to")
    private LocalDateTime visibleTo;

    @PrePersist
    public void prePersist() {
        if (noticeType == null) noticeType = NoticeType.GENERAL;
        if (targetRole == null) targetRole = TargetRole.ALL;
        if (status     == null) status     = NoticeStatus.DRAFT;
        if (createdAt  == null) createdAt  = LocalDateTime.now();
    }

    public void update(
            String title,
            String content,
            boolean isPinned,
            NoticeType noticeType,
            TargetRole targetRole,
            NoticeStatus status,
            LocalDateTime visibleFrom,
            LocalDateTime visibleTo
    ) {
        this.title      = title;
        this.content    = content;
        this.isPinned   = isPinned;
        this.noticeType = noticeType != null ? noticeType : NoticeType.GENERAL;
        this.targetRole = targetRole != null ? targetRole : TargetRole.ALL;
        this.status     = status;
        this.visibleFrom = visibleFrom;
        this.visibleTo   = visibleTo;
    }

    public void updateStatus(NoticeStatus status) {
        this.status = status;
    }

    public void softDelete() {
        this.status = NoticeStatus.DELETED;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
