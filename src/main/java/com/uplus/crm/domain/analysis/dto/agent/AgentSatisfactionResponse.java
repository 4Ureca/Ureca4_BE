package com.uplus.crm.domain.analysis.dto.agent;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentSatisfactionResponse {
  private String empId;
  private LocalDate startedAt;
  private LocalDate endedAt;
  private Double satisfactionScore;
  private Double teamAvgSatisfactionScore;
  private Double responseRate;      // 응답률 (%)
}