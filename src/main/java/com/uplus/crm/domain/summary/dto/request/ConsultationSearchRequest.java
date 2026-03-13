package com.uplus.crm.domain.summary.dto.request;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class ConsultationSearchRequest {

  @Size(max = 300)
  private String keyword;
  private String agentId;
  private String agentName;
  private String channel;
  private String categoryCode;
  private String categoryLarge;
  private String categoryMedium;
  private String categorySmall;
  private String customerId;
  private String customerName;
  private String customerPhone;
  private String grade;
  private String gender;
  private Boolean intent;
  private Boolean defenseAttempted;
  private Boolean defenseSuccess;
  private List<String> riskType;
  private List<String> riskLevel;
  private List<String> productCode;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fromDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate toDate;

  private Integer minDuration;
  private Integer maxDuration;
}
