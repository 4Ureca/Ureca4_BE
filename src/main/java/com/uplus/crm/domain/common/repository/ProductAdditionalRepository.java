package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.ProductAdditional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAdditionalRepository extends JpaRepository<ProductAdditional, String> {
  List<ProductAdditional> findTop20ByAdditionalNameContainingOrAdditionalCodeContaining(String name, String code);
  List<ProductAdditional> findByAdditionalCodeIn(List<String> additionalCodes);
}