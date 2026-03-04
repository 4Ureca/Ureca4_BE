package com.uplus.crm.domain.consultation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConsultationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consult_id")
    private Long consultId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "channel", nullable = false, columnDefinition = "ENUM('CALL','CHATTING')")
    private String channel;

    @Column(name = "category_code", nullable = false, length = 20)
    private String categoryCode;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "iam_issue", columnDefinition = "TEXT")
    private String iamIssue;

    @Column(name = "iam_action", columnDefinition = "TEXT")
    private String iamAction;

    @Column(name = "iam_memo", columnDefinition = "TEXT")
    private String iamMemo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
