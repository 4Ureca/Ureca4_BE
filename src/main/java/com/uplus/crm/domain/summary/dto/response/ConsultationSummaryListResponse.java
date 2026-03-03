package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryListResponse {

  private Long consultId;
  private LocalDateTime consultedAt;

  private String customerName;
  private String customerType;

  private String categoryLarge;
  private String categoryMedium;
  private String categorySmall;

  private Long agentId;
  private String agentName;

  public static ConsultationSummaryListResponse from(
      ConsultationSummary e) {

    return ConsultationSummaryListResponse.builder()
        .consultId(e.getConsultId())
        .consultedAt(e.getConsultedAt())
        .customerName(e.getCustomer() != null ? e.getCustomer().getName() : null)
        .customerType(e.getCustomer() != null ? e.getCustomer().getType() : null)
        .categoryLarge(e.getCategory() != null ? e.getCategory().getLarge() : null)
        .categoryMedium(e.getCategory() != null ? e.getCategory().getMedium() : null)
        .categorySmall(e.getCategory() != null ? e.getCategory().getSmall() : null)
        .agentId(e.getAgent() != null ? e.getAgent().get_id() : null)
        .agentName(e.getAgent() != null ? e.getAgent().getName() : null)
        .build();
  }
}