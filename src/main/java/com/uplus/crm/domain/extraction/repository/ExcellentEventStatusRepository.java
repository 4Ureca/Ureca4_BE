package com.uplus.crm.domain.extraction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uplus.crm.domain.extraction.entity.ExcellentEventStatus;

public interface ExcellentEventStatusRepository extends JpaRepository<ExcellentEventStatus, Long> {
	List<ExcellentEventStatus> findAllByConsultIdIn(List<Long> consultIds);
}
