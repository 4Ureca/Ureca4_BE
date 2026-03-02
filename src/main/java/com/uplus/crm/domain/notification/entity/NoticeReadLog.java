package com.uplus.crm.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notice_read_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rl_notice_emp",
                columnNames = {"notice_id", "emp_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NoticeReadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "notice_id", nullable = false)
    private int noticeId;

    @Column(name = "emp_id", nullable = false)
    private int empId;

    @Column(name = "read_at", nullable = false, updatable = false)
    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (readAt == null) readAt = LocalDateTime.now();
    }
}
