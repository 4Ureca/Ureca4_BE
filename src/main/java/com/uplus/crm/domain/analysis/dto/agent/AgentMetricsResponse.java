package com.uplus.crm.domain.analysis.dto.agent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentMetricsResponse {
  private String agentName;
  private String empId;

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
