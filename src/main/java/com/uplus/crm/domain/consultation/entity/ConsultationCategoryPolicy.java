package com.uplus.crm.domain.consultation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultation_category_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationCategoryPolicy {

    @Id
    @Column(name = "category_code", length = 20)
    private String categoryCode;

    @Column(name = "large_category", nullable = false, length = 50)
    private String largeCategory;

    @Column(name = "medium_category", nullable = false, length = 50)
    private String mediumCategory;

    @Column(name = "small_category", nullable = false, length = 50)
    private String smallCategory;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
