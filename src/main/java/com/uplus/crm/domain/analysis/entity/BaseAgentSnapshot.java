package com.uplus.crm.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public abstract class BaseAgentSnapshot {

  @Id
  private String id;

  private Long agentId;

  // String 대신 LocalDateTime으로 수정 완료!
  private LocalDateTime startAt;
  private LocalDateTime endAt;

  private Integer consultCount;
  private Double avgDurationMinutes;
  private Double iamKeywordMatchAnalysis; // IAM 키워드 일치율

  // 카테고리 순위 데이터
  private List<CategoryRanking> categoryRanking;

  // 고객 만족도 분석 데이터
  private CustomerSatisfactionAnalysis customerSatisfactionAnalysis;


  // --- 내부 객체 구조  ---
  @Getter
  public static class CategoryRanking {
    private String large;
    private String medium;
    private Integer count;
  }

  @Getter
  public static class CustomerSatisfactionAnalysis {
    private Double satisfactionScore;
    private Double responseRate;
    private Integer surveyTotalCount;
    private Integer surveyResponseCount;
  }
}