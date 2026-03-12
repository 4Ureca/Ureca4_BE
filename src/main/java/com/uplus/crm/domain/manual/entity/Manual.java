package com.uplus.crm.domain.manual.entity;

import java.time.LocalDateTime;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "manuals")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class Manual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", nullable = false)
    private ConsultationCategoryPolicy categoryPolicy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}