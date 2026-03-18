package com.uplus.crm.domain.manual.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uplus.crm.domain.manual.entity.Manual;
import java.util.Optional;

public interface ManualRepository extends JpaRepository<Manual, Integer> {
    
    // 기존 활성화 매뉴얼 찾기용 (상태 변경 로직용)
    Optional<Manual> findByCategoryPolicy_CategoryCodeAndIsActiveTrue(String categoryCode);

    // 1. 전체 조회 (상태 필터링 포함) 🥊
    Page<Manual> findAllByIsActive(Boolean isActive, Pageable pageable);
    
    // 2. 전체 조회 (필터링 없음) 🥊
    Page<Manual> findAll(Pageable pageable);

    // 3. 카테고리별 조회 (상태 필터링 포함) 🥊
    Page<Manual> findAllByCategoryPolicy_CategoryCodeAndIsActive(String categoryCode, Boolean isActive, Pageable pageable);

    // 4. 카테고리별 조회 (상태 필터링 없음) 🥊
    Page<Manual> findAllByCategoryPolicy_CategoryCode(String categoryCode, Pageable pageable);
}