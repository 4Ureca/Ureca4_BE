package com.uplus.crm.domain.analysis.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "처리 카테고리 순위 및 건수 정보")
public class CategoryRankingDto {
  private String empId;
  private LocalDate startedAt;
  private LocalDate endedAt;

  private String name;        // 대분류명 (예: "일반 문의")
  private Integer totalCount; // 대분류 총 건수 (예: 18건)
  private List<MediumCategoryDto> mediumCategories; // 중분류 리스트

  @Getter
  @Builder
  public static class MediumCategoryDto {
    private String name;   // 중분류명 (예: "요금 문의")
    private Integer count; // 중분류 건수 (예: 8건)
  }


}