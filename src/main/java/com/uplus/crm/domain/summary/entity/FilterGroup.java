package com.uplus.crm.domain.summary.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.uplus.crm.domain.account.entity.Employee;

/**
 * 커스텀 필터 그룹 (테이블 31)
 * - 필터 그룹을 묶는 역할
 * - CRUD 대상
 * - 삭제 시 hard delete (실제 DELETE, filter_custom도 cascade 삭제)
 */
@Entity
@Table(name = "filter_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FilterGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "filter_group_id")
    private Integer filterGroupId;

    @Column(name = "emp_id", nullable = false)
    private Integer empId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "filterGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FilterCustom> filterCustoms = new ArrayList<>();

    @Builder
    public FilterGroup(Integer empId, String groupName, Integer sortOrder) {
        this.empId = empId;
        this.groupName = groupName;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    // --- 비즈니스 메서드 ---

    public void updateGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void clearFilters() {
        this.filterCustoms.clear();
    }

    public void addFilterCustom(FilterCustom filterCustom) {
        this.filterCustoms.add(filterCustom);
        filterCustom.setFilterGroup(this);
    }
}