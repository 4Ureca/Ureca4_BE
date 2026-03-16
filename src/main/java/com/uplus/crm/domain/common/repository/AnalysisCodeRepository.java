package com.uplus.crm.domain.common.repository;

import com.uplus.crm.domain.common.entity.AnalysisCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisCodeRepository extends JpaRepository<AnalysisCode, Long> {

    List<AnalysisCode> findByClassification(String classification);
}
