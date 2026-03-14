package com.uplus.crm.domain.analysis.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Getter
@NoArgsConstructor
public abstract class BaseTotalSnapshot {
  @Id
  private String id;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private Double avgDurationMinutes;
  private Integer totalConsultCount;

  private PerformanceSummary performanceSummary;

  @Getter
  public static class PerformanceSummary {
    private Double avgConsultPerAgent;
    private Double avgSatisfiedScore;
    private Double avgQualityScore;
  }

  public Double getAvgSatisfiedScore() {
    return (performanceSummary != null) ? performanceSummary.getAvgSatisfiedScore() : 0.0;
  }

  public Double getAvgQualityScore() {
    return (performanceSummary != null) ? performanceSummary.getAvgQualityScore() : 0.0;
  }

  public Double getAvgConsultPerAgent() {
    return (performanceSummary != null) ? performanceSummary.getAvgConsultPerAgent() : 0.0;
  }
}