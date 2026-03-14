package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.RiskTypePolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskTypePolicyRepository extends JpaRepository<RiskTypePolicy, String> {
  List<RiskTypePolicy> findByIsActiveTrue();
}
