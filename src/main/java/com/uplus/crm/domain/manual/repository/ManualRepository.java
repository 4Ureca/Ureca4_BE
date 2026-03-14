package com.uplus.crm.domain.manual.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.manual.entity.Manual;

public interface ManualRepository extends JpaRepository<Manual, Integer> {
	// Service 40번 라인 해결
    Optional<Manual> findByCategoryPolicy_CategoryCodeAndIsActiveTrue(String categoryCode);

    // Service 85번 라인 해결
    List<Manual> findAllByOrderByCreatedAtDesc();

    // Service 88번 라인 해결
    List<Manual> findAllByCategoryPolicy_CategoryCodeOrderByCreatedAtDesc(String categoryCode);
}
