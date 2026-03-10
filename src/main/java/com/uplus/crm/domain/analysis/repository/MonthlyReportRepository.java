package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.MonthlyReportSnapshot;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonthlyReportRepository extends MongoRepository<MonthlyReportSnapshot, String> {
  Optional<MonthlyReportSnapshot> findByStartAt(LocalDateTime startAt);
}
