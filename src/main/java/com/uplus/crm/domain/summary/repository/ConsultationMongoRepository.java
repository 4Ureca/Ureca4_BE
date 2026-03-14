package com.uplus.crm.domain.summary.repository;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsultationMongoRepository
    extends MongoRepository<ConsultationSummary, String> {

  List<ConsultationSummary> findByConsultIdIn(Collection<Long> consultIds);
}
