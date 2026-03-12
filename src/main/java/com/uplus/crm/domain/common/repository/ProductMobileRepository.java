package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.ProductMobile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMobileRepository extends JpaRepository<ProductMobile, String> {
  List<ProductMobile> findTop20ByPlanNameContainingOrMobileCodeContaining(String name, String code);
  List<ProductMobile> findByMobileCodeIn(List<String> productCodes);
}
