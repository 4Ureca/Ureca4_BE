package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SummaryRepository
    extends MongoRepository<ConsultationSummary, String> {

  Page<ConsultationSummary> findAll(Pageable pageable);

  Optional<ConsultationSummary> findByConsultId(Long consultId);
}