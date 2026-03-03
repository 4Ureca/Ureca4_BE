package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryDetailResponse {

  private Long consultId;
  private LocalDateTime consultedAt;
  private LocalDateTime createdAt;
  private Integer durationSec;
  private String channel;

  private CategoryInfo category;
  private AgentInfo agent;
  private CustomerInfo customer;
  private IamInfo iam;
  private CancellationInfo cancellation;
  private List<ProductInfo> products;

  @Getter @Builder
  public static class CategoryInfo {
    private String large;
    private String medium;
    private String small;
  }

  @Getter @Builder
  public static class AgentInfo {
    private Long id;
    private String name;
  }

  @Getter @Builder
  public static class CustomerInfo {
    private String grade;
    private String name;
    private String phone;
    private String type;
    private String ageGroup;
    private String satisfaction;
  }

  @Getter @Builder
  public static class IamInfo {
    private String memo;
    private String action;
    private String issue;
    private Double matchRate;
  }

  @Getter @Builder
  public static class CancellationInfo {
    private Boolean intent;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private List<String> defenseActions;
    private String complaintReasons;
  }

  @Getter @Builder
  public static class ProductInfo {
    private List<String> subscribed;
    private List<String> canceled;
    private List<ConsultationSummary.ResultProducts.Conversion> conversion;
    private List<String> recommitment;
    private String changeType;
  }

  public static ConsultationSummaryDetailResponse from(
      ConsultationSummary e) {

    String satisfaction = null;

    if (e.getCustomer() != null) {
      Double score = e.getCustomer().getSatisfiedScore();
      satisfaction = score == null ? "미작성" : score + "";
    }

    return ConsultationSummaryDetailResponse.builder()
        .consultId(e.getConsultId())
        .consultedAt(e.getConsultedAt())
        .createdAt(e.getCreatedAt())
        .durationSec(e.getDurationSec())
        .channel(e.getChannel())

        .category(e.getCategory() == null ? null :
            CategoryInfo.builder()
                .large(e.getCategory().getLarge())
                .medium(e.getCategory().getMedium())
                .small(e.getCategory().getSmall())
                .build())

        .agent(e.getAgent() == null ? null :
            AgentInfo.builder()
                .id(e.getAgent().get_id())
                .name(e.getAgent().getName())
                .build())

        .customer(e.getCustomer() == null ? null :
            CustomerInfo.builder()
                .grade(e.getCustomer().getGrade())
                .name(e.getCustomer().getName())
                .phone(e.getCustomer().getPhone())
                .type(e.getCustomer().getType())
                .ageGroup(e.getCustomer().getAgeGroup())
                .satisfaction(satisfaction)
                .build())

        .iam(e.getIam() == null ? null :
            IamInfo.builder()
                .memo(e.getIam().getMemo())
                .action(e.getIam().getAction())
                .issue(e.getIam().getIssue())
                .matchRate(e.getIam().getMatchRates())
                .build())

        .cancellation(e.getCancellation() == null ? null :
            CancellationInfo.builder()
                .intent(e.getCancellation().getIntent())
                .defenseAttempted(e.getCancellation().getDefenseAttempted())
                .defenseSuccess(e.getCancellation().getDefenseSuccess())
                .defenseActions(e.getCancellation().getDefenseActions())
                .complaintReasons(e.getCancellation().getComplaintReasons())
                .build())

        .products(e.getResultProducts() == null ? null :
            e.getResultProducts().stream()
                .map(p -> ProductInfo.builder()
                    .subscribed(p.getSubscribed())
                    .canceled(p.getCanceled())
                    .conversion(p.getConversion())
                    .recommitment(p.getRecommitment())
                    .changeType(p.getChangeType())
                    .build())
                .toList())

        .build();
  }
}