package com.uplus.crm.domain.consultation.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationCategoryRepository extends JpaRepository<ConsultationCategoryPolicy, String> {
}
