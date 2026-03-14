package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryDto {

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  private Long consultId;
  private LocalDateTime consultedAt;
  private String channel;
  private String customerName;
  private String phone;
  private String grade;
  private String agentName;
  private String categoryLarge;
  private String categoryMedium;
  private String categorySmall;
  private String summaryContent;
  private Boolean intent;
  private Boolean defenseSuccess;
  private List<RiskFlagDto> riskFlags;

  @Getter
  @Builder
  public static class RiskFlagDto {
    private String riskType;
    private String riskLevel;
  }

  public static ConsultationSummaryDto from(ConsultationSummary summary) {
    return ConsultationSummaryDto.builder()
        .consultId(summary.getConsultId())
        .consultedAt(convertUtcToKst(summary.getConsultedAt()))
        .channel(summary.getChannel())
        .customerName(summary.getCustomer() != null ? summary.getCustomer().getName() : null)
        .phone(summary.getCustomer() != null ? summary.getCustomer().getPhone() : null)
        .grade(summary.getCustomer() != null ? summary.getCustomer().getGrade() : null)
        .agentName(summary.getAgent() != null ? summary.getAgent().getName() : null)
        .categoryLarge(summary.getCategory() != null ? summary.getCategory().getLarge() : null)
        .categoryMedium(summary.getCategory() != null ? summary.getCategory().getMedium() : null)
        .categorySmall(summary.getCategory() != null ? summary.getCategory().getSmall() : null)
        .summaryContent(summary.getSummary() != null && summary.getSummary().getContent() != null
            ? summary.getSummary().getContent()
            : "")
        .intent(summary.getCancellation() != null ? summary.getCancellation().getIntent() : null)
        .defenseSuccess(summary.getCancellation() != null
            ? summary.getCancellation().getDefenseSuccess()
            : null)
        .riskFlags(summary.getRiskFlags() == null
            ? Collections.emptyList()
            : summary.getRiskFlags().stream()
                .map(riskFlag -> RiskFlagDto.builder()
                    .riskType(riskFlag.getRiskType())
                    .riskLevel(riskFlag.getRiskLevel())
                    .build())
                .toList())
        .build();
  }

  private static LocalDateTime convertUtcToKst(LocalDateTime utcDateTime) {
    if (utcDateTime == null) {
      return null;
    }

    return utcDateTime.atZone(ZoneOffset.UTC)
        .withZoneSameInstant(KST_ZONE)
        .toLocalDateTime();
  }
}
