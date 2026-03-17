package com.uplus.crm.domain.extraction.repository;

import com.uplus.crm.domain.extraction.entity.SummaryEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryEventStatusRepository extends JpaRepository<SummaryEventStatus, Long> {
}
