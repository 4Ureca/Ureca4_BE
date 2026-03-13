package com.uplus.crm.domain.summary.dto.response;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultationSummaryDetailResponse {

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  private String id;
  private Long consultId;
  private LocalDateTime consultedAt;
  private LocalDateTime createdAt;
  private Integer durationSec;
  private String channel;

  private CategoryInfo category;
  private AgentInfo agent;
  private CustomerInfo customer;
  private IamInfo iam;
  private SummaryInfo summary;
  private List<RiskFlagInfo> riskFlags;
  private CancellationInfo cancellation;

  private List<ResultProductInfo> resultProducts;

  @Getter
  @Builder
  public static class CategoryInfo {
    private String code;
    private String large;
    private String medium;
    private String small;
  }

  @Getter
  @Builder
  public static class AgentInfo {
    private Long id;
    private String name;
  }

  @Getter
  @Builder
  public static class CustomerInfo {
    private Long id;
    private String grade;
    private String name;
    private String phone;
    private String type;
    private String ageGroup;
    private Double satisfiedScore;
  }

  @Getter
  @Builder
  public static class IamInfo {
    private String memo;
    private String action;
    private String issue;
    private List<String> matchKeyword;
    private Double matchRate;
  }

  @Getter
  @Builder
  public static class SummaryInfo {
    private String content;
    private List<String> keywords;
  }

  @Getter
  @Builder
  public static class RiskFlagInfo {
    private String riskType;
    private String riskLevel;
  }

  @Getter
  @Builder
  public static class CancellationInfo {
    private Boolean intent;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private List<String> defenseActions;
    private String complaintReasons;
  }

  @Getter
  @Builder
  public static class ResultProductInfo {
    private String changeType;
    private List<ProductAction> products;
  }

  @Getter
  @Builder
  public static class ProductAction {
    private ProductInfo before;
    private ProductInfo after;
  }

  @Getter
  @Builder
  public static class ProductInfo {
    private String code;
    private String name;
  }

  public static ConsultationSummaryDetailResponse from(
      ConsultationSummary e,
      Map<String, String> productNameMap) {

    return ConsultationSummaryDetailResponse.builder()
        .id(e.getId())
        .consultId(e.getConsultId())
        .consultedAt(convertUtcToKst(e.getConsultedAt()))
        .createdAt(convertUtcToKst(e.getCreatedAt()))
        .durationSec(e.getDurationSec())
        .channel(e.getChannel())

        .category(e.getCategory() == null ? null :
            CategoryInfo.builder()
                .code(e.getCategory().getCode())
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
                .id(e.getCustomer().get_id())
                .grade(e.getCustomer().getGrade())
                .name(e.getCustomer().getName())
                .phone(e.getCustomer().getPhone())
                .type(e.getCustomer().getType())
                .ageGroup(e.getCustomer().getAgeGroup())
                .satisfiedScore(e.getCustomer().getSatisfiedScore())
                .build())

        .iam(e.getIam() == null ? null :
            IamInfo.builder()
                .memo(e.getIam().getMemo())
                .action(e.getIam().getAction())
                .issue(e.getIam().getIssue())
                .matchKeyword(e.getIam().getMatchKeyword())
                .matchRate(e.getIam().getMatchRates())
                .build())

        .summary(e.getSummary() == null ? null :
            SummaryInfo.builder()
                .content(e.getSummary().getContent())
                .keywords(e.getSummary().getKeywords())
                .build())

        .riskFlags(e.getRiskFlags() == null ? null :
            e.getRiskFlags().stream()
                .map(flag -> RiskFlagInfo.builder()
                    .riskType(flag.getRiskType())
                    .riskLevel(flag.getRiskLevel())
                    .build())
                .toList())

        .cancellation(e.getCancellation() == null ? null :
            CancellationInfo.builder()
                .intent(e.getCancellation().getIntent())
                .defenseAttempted(e.getCancellation().getDefenseAttempted())
                .defenseSuccess(e.getCancellation().getDefenseSuccess())
                .defenseActions(e.getCancellation().getDefenseActions())
                .complaintReasons(e.getCancellation().getComplaintReasons())
                .build())

        .resultProducts(buildProductInfo(e, productNameMap))
        .build();
  }

  private static List<ResultProductInfo> buildProductInfo(
      ConsultationSummary summary,
      Map<String, String> productNameMap) {

    if (summary.getResultProducts() == null) {
      return null;
    }

    return summary.getResultProducts().stream()
        .map(p -> ResultProductInfo.builder()
            .changeType(p.getChangeType())
            .products(buildActions(p, productNameMap))
            .build())
        .toList();
  }

  private static List<ProductAction> buildActions(
      ConsultationSummary.ResultProducts p,
      Map<String, String> productNameMap) {

    if ("CHANGE".equals(p.getChangeType()) && p.getConversion() != null) {
      return p.getConversion().stream()
          .map(c -> ProductAction.builder()
              .before(productInfo(c.getCanceled(), productNameMap))
              .after(productInfo(c.getSubscribed(), productNameMap))
              .build())
          .toList();
    }

    if (p.getSubscribed() != null) {
      return p.getSubscribed().stream()
          .map(code -> ProductAction.builder()
              .before(null)
              .after(productInfo(code, productNameMap))
              .build())
          .toList();
    }

    if (p.getCanceled() != null) {
      return p.getCanceled().stream()
          .map(code -> ProductAction.builder()
              .before(productInfo(code, productNameMap))
              .after(null)
              .build())
          .toList();
    }

    if (p.getRecommitment() != null) {
      return p.getRecommitment().stream()
          .map(code -> ProductAction.builder()
              .before(null)
              .after(productInfo(code, productNameMap))
              .build())
          .toList();
    }

    return List.of();
  }

  private static ProductInfo productInfo(
      String code,
      Map<String, String> productNameMap) {

    if (code == null) {
      return null;
    }

    return ProductInfo.builder()
        .code(code)
        .name(productNameMap.get(code))
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