package com.uplus.crm.domain.analysis.dto.agent;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentMetricsResponse {
  private String empId;
  private LocalDate startedAt;
  private LocalDate endedAt;

  // 내 지표
  private Integer myConsultCount;
  private String myAvgDuration; // "M:SS" 포맷
  private Double myQualityScore;
  private Double mySatisfactionScore;
  private Double iamMatchRate; //

  // 팀 평균 지표
  private Double teamAvgConsultCount;
  private String teamAvgDuration;
  private Double teamAvgQualityScore;
  private Double teamAvgSatisfactionScore;
}
