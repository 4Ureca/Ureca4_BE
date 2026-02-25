package com.uplus.crm.domain.summary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 커스텀 필터 항목 (테이블 32)
 * - 필터 그룹 안의 실제 조건 값
 * - FilterGroup 삭제 시 cascade로 함께 hard delete됨
 */
@Entity
@Table(name = "filter_custom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterCustom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_custom_id")
    private Integer filterCustomId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_group_id", nullable = false)
    private FilterGroup filterGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    private Filter filter;

    @Column(name = "filter_value", nullable = false, columnDefinition = "TEXT")
    private String filterValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public FilterCustom(Filter filter, String filterValue) {
        this.filter = filter;
        this.filterValue = filterValue;
    }
}