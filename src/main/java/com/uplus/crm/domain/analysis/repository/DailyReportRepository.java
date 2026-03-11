package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.DailyReportSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends MongoRepository<DailyReportSnapshot, String> {

  Optional<DailyReportSnapshot> findByStartAt(LocalDateTime startAt);
}