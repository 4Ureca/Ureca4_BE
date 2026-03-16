package com.uplus.crm.domain.summary.document;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "consultation_summary")
@CompoundIndex(name = "idx_consultedAt_agentId",
    def = "{'consultedAt':-1,'agent.id':1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationSummary {

  @Id
  private String id;

  @Indexed(name = "uq_consultId", unique = true)
  private Long consultId;
  @Indexed(name = "idx_consultedAt", direction = IndexDirection.DESCENDING)
  private LocalDateTime consultedAt;
  private String channel;
  private Integer durationSec;

  private Agent agent;
  private Category category;
  private Iam iam;
  private Summary summary;
  private List<RiskFlag> riskFlags;
  private Customer customer;
  private Cancellation cancellation;
  private Outbound outbound;
  private List<ResultProducts> resultProducts;

  private LocalDateTime createdAt;

  @Getter
  @Builder
  public static class Agent {
    private Long _id;
    private String name;
  }

  @Getter
  @Builder
  public static class Category {
    private String code;
    private String large;
    private String medium;
    private String small;
  }

  @Getter
  @Builder
  public static class Iam {
    private String issue;
    private String action;
    private String memo;
    private List<String> matchKeyword;
    private Double matchRates;
  }

  @Getter
  @Builder
  public static class Summary {
    private String status;
    private String content;
    private List<String> keywords;
  }

  @Getter
  @Builder
  public static class Customer {
    private Long _id;
    private String type;
    private String phone;
    private String name;
    private String ageGroup;
    private String gender;
    private String grade;
    private Double satisfiedScore;
  }

  @Getter
  @Builder
  public static class Cancellation {
    private Boolean intent;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private List<String> defenseActions;
    private String defenseCategory;
    private String complaintReasons;
    private String complaintCategory;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Outbound {
    private String callResult;
    private String rejectReason;
    private String outboundReport;
  }

  @Getter
  @Builder
  public static class RiskFlag {
    private String riskType;
    private String riskLevel;
  }

  @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
  public static class ResultProducts {
    private List<String> subscribed;
    private List<String> canceled;
    private List<Conversion> conversion;
    private List<String> recommitment;
    private String changeType;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Conversion {
      private String subscribed;
      private String canceled;
    }
  }
}
