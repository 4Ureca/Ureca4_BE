package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.MonthlyAgentReportSnapshot;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MonthlyAgentReportRepository extends
    MongoRepository<MonthlyAgentReportSnapshot, String> {
  Optional<MonthlyAgentReportSnapshot> findByAgentIdAndStartAt(Long agentId, LocalDateTime startAt);
}