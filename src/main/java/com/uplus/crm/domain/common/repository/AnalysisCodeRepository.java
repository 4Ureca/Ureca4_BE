package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.AnalysisCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisCodeRepository extends JpaRepository<AnalysisCode, Long> {

  List<AnalysisCode> findByClassification(String classification);
}
