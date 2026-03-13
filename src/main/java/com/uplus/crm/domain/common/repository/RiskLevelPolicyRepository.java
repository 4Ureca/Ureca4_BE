package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.RiskLevelPolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskLevelPolicyRepository extends JpaRepository<RiskLevelPolicy, String> {
  List<RiskLevelPolicy> findAllByOrderBySortOrder();
}