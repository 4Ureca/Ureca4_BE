package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryConsultationResultRepository
    extends JpaRepository<ConsultationResult, Long> {
}