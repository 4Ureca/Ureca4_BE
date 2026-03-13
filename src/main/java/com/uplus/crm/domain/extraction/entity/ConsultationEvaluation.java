package com.uplus.crm.domain.extraction.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "consultation_evaluations", indexes = {
    @Index(name = "idx_eval_candidate_status", columnList = "isCandidate, selectionStatus")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString
public class ConsultationEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evaluationId;

    @Column(nullable = false)
    private Long consultId;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String evaluationReason;

    @Column(nullable = false)
    private boolean isCandidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SelectionStatus selectionStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ConsultationEvaluation(Long consultId, Integer score, String evaluationReason, boolean isCandidate) {
        this.consultId = consultId;
        this.score = score;
        this.evaluationReason = evaluationReason;
        this.isCandidate = isCandidate;
        this.selectionStatus = SelectionStatus.PENDING; // 기본값은 항상 '검토 대기'
    }
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateSelectionStatus(SelectionStatus status) {
        this.selectionStatus = status;
        this.updatedAt = LocalDateTime.now(); 
    }
}