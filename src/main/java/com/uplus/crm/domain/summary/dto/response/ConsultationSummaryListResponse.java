package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryListResponse {

  private Long consultId;
  private LocalDateTime consultedAt;
  private String channel;

  private String customerName;
  private String customerType;
  private String customerGrade;

  private String categoryCode;
  private String categoryLarge;
  private String categoryMedium;
  private String categorySmall;

  private Long agentId;
  private String agentName;

  /** 위험 유형 태그 목록 (예: [해지위험, 반복민원]) */
  private List<String> riskFlags;

  public static ConsultationSummaryListResponse from(ConsultationSummary e) {
    return ConsultationSummaryListResponse.builder()
        .consultId(e.getConsultId())
        .consultedAt(e.getConsultedAt())
        .channel(e.getChannel())
        .customerName(e.getCustomer() != null ? e.getCustomer().getName() : null)
        .customerType(e.getCustomer() != null ? e.getCustomer().getType() : null)
        .customerGrade(e.getCustomer() != null ? e.getCustomer().getGrade() : null)
        .categoryCode(e.getCategory() != null ? e.getCategory().getCode() : null)
        .categoryLarge(e.getCategory() != null ? e.getCategory().getLarge() : null)
        .categoryMedium(e.getCategory() != null ? e.getCategory().getMedium() : null)
        .categorySmall(e.getCategory() != null ? e.getCategory().getSmall() : null)
        .agentId(e.getAgent() != null ? e.getAgent().get_id() : null)
        .agentName(e.getAgent() != null ? e.getAgent().getName() : null)
        .riskFlags(e.getRiskFlags())
        .build();
  }
}