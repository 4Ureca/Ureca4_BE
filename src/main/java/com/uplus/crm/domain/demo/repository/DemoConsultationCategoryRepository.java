package com.uplus.crm.domain.demo.repository;

import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoConsultationCategoryRepository extends JpaRepository<ConsultationCategoryPolicy, String> {
}
