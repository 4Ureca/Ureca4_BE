package com.uplus.crm.domain.manual.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.manual.entity.Manual;

public interface ManualRepository extends JpaRepository<Manual, Integer> {
    Optional<Manual> findByCategoryPolicy_CategoryCodeAndIsActiveTrue(String categoryCode);
    List<Manual> findAllByOrderByCreatedAtDesc();
    List<Manual> findAllByCategoryPolicy_CategoryCodeOrderByCreatedAtDesc(String categoryCode);
}
