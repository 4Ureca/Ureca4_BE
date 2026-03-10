package com.uplus.crm.domain.analysis.repository;

import com.uplus.crm.domain.analysis.entity.DailyReportSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DailyReportRepository extends MongoRepository<DailyReportSnapshot, String> {

  /**
   * 특정 날짜의 전체(팀) 평균 스냅샷 조회
   * @param startAt 집계 시작 시각 (해당일 00:00:00)
   * @return 전체 평균 데이터가 담긴 스냅샷
   */
  Optional<DailyReportSnapshot> findByStartAt(LocalDateTime startAt);
}