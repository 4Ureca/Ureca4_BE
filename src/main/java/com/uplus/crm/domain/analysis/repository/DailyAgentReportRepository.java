package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.DailyAgentReportSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DailyAgentReportRepository extends MongoRepository<DailyAgentReportSnapshot, String> {
  // 상담사 ID와 날짜로 데이터 찾기
  Optional<DailyAgentReportSnapshot> findByAgentIdAndStartAt(Long agentId, LocalDateTime startAt);
}