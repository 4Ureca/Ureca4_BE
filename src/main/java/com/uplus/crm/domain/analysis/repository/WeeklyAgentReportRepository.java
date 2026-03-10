package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.WeeklyAgentReportSnapshot;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WeeklyAgentReportRepository extends
    MongoRepository<WeeklyAgentReportSnapshot, String> {
  Optional<WeeklyAgentReportSnapshot> findByAgentIdAndStartAt(Long agentId, LocalDateTime startAt);
}
