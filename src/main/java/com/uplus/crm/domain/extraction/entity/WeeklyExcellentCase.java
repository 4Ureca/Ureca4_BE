package com.uplus.crm.domain.extraction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_excellent_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WeeklyExcellentCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long snapshotId;

    @Column(name = "consult_id", nullable = false)
    private Long consultId;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column(name = "year_val", nullable = false)
    private Integer yearVal;

    @Column(name = "week_val", nullable = false)
    private Integer weekVal;

    @Column(name = "admin_reason", columnDefinition = "TEXT")
    private String adminReason;

    @Column(name = "selected_at", updatable = false)
    private LocalDateTime selectedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}