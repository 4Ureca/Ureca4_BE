package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConsultationCategoryPolicyRepository extends JpaRepository<ConsultationCategoryPolicy, String> {

  List<ConsultationCategoryPolicy> findByIsActiveTrueOrderBySortOrder();

  @Query("""
        SELECT c FROM ConsultationCategoryPolicy c
        WHERE c.isActive = true
        AND (
            c.categoryCode LIKE %:keyword% OR
            c.largeCategory LIKE %:keyword% OR
            c.mediumCategory LIKE %:keyword% OR
            c.smallCategory LIKE %:keyword%
        )
        ORDER BY c.sortOrder
        """)
  List<ConsultationCategoryPolicy> searchByKeyword(@Param("keyword") String keyword);
}