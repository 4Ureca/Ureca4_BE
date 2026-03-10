package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.WeeklyReportSnapshot;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WeeklyReportRepository extends MongoRepository<WeeklyReportSnapshot, String> {
  Optional<WeeklyReportSnapshot> findByStartAt(LocalDateTime startAt);
}
