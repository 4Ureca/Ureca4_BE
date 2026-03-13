package com.uplus.crm.domain.manual.entity;

import java.time.LocalDateTime;

import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manuals")
@Getter 
@Setter // Service에서 setTitle, setContent 등을 사용하기 위해 필수
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Manual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_id")
    private Integer manualId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Boolean isActive;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private ConsultationCategoryPolicy categoryPolicy; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id")
    private Employee employee; 
}