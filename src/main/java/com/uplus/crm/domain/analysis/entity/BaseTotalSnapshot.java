package com.uplus.crm.domain.analysis.entity;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
public abstract class BaseTotalSnapshot {
  @Id
  private String id;

  private LocalDate startAt;
  private LocalDate endAt;

  private Double avgConsultCountPerAgent; // 팀 평균 상담 건수
  private Double avgDurationMinutes;      // 팀 평균 상담 시간
  private Double avgSatisfiedScore;       // 팀 평균 만족도
  private Double qualityScore;            // 팀 평균 품질 점수

  private Integer totalConsultCount;      // 전체 상담 총합
}