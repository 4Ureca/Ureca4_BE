package com.uplus.crm.domain.analysis.dto.agent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentSatisfactionResponse {
  private Double satisfactionScore;
  private Double teamAvgSatisfactionScore;
  private Double responseRate;      // 응답률 (%)
}