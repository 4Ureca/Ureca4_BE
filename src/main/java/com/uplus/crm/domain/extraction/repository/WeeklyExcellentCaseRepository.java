package com.uplus.crm.domain.extraction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.extraction.entity.WeeklyExcellentCase;

public interface WeeklyExcellentCaseRepository extends JpaRepository<WeeklyExcellentCase, Long> {
	boolean existsByConsultId(Long consultId);
}