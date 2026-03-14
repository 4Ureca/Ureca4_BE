package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.ProductHome;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductHomeRepository extends JpaRepository<ProductHome, String> {
  List<ProductHome> findTop20ByProductNameContainingOrHomeCodeContaining(String name, String code);
  List<ProductHome> findByHomeCodeIn(List<String> productCodes);
}