package com.uplus.crm.domain.extraction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "retention_analysis")
@Getter
@NoArgsConstructor
public class RetentionAnalysis {
    @Id
    private Long consultId; 

    @Column(columnDefinition = "TEXT")
    private String rawSummary; 
}