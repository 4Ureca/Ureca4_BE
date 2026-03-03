package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.demo.entity.ConsultationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationResultRepository
    extends JpaRepository<ConsultationResult, Long> {
}