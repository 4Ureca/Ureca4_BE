package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.summary.entity.FilterGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterGroupRepository extends JpaRepository<FilterGroup, Integer> {

    /**
     * 특정 직원의 필터 그룹 목록 (정렬 순서대로)
     */
    List<FilterGroup> findAllByEmpIdOrderBySortOrderAsc(Integer empId);

    /**
     * 특정 직원의 필터 그룹 목록 (ID 목록 기준)
     */
    List<FilterGroup> findAllByFilterGroupIdInAndEmpId(
            List<Integer> filterGroupIds, Integer empId);
}