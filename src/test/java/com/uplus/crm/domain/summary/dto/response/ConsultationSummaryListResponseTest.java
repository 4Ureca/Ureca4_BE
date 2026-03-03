package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationSummaryListResponseTest {

  @Test
  @DisplayName("정상 매핑 테스트 - 모든 값 존재")
  void from_fullMapping_success() {
    // given
    ConsultationSummary summary = new ConsultationSummary();

    summary.setConsultId(200L);
    summary.setConsultedAt(LocalDateTime.of(2026, 3, 2, 14, 30));

    summary.setCustomer(
        ConsultationSummary.Customer.builder()
            .name("김고객")
            .type("개인")
            .build()
    );

    summary.setCategory(
        ConsultationSummary.Category.builder()
            .large("요금")
            .medium("할인")
            .small("재약정")
            .build()
    );

    summary.setAgent(
        ConsultationSummary.Agent.builder()
            ._id(30L)
            .name("이상담")
            .build()
    );

    // when
    ConsultationSummaryListResponse response =
        ConsultationSummaryListResponse.from(summary);

    // then
    assertThat(response.getConsultId()).isEqualTo(200L);
    assertThat(response.getConsultedAt())
        .isEqualTo(LocalDateTime.of(2026, 3, 2, 14, 30));

    assertThat(response.getCustomerName()).isEqualTo("김고객");
    assertThat(response.getCustomerType()).isEqualTo("개인");

    assertThat(response.getCategoryLarge()).isEqualTo("요금");
    assertThat(response.getCategoryMedium()).isEqualTo("할인");
    assertThat(response.getCategorySmall()).isEqualTo("재약정");

    assertThat(response.getAgentId()).isEqualTo(30L);
    assertThat(response.getAgentName()).isEqualTo("이상담");
  }

  @Test
  @DisplayName("하위 객체가 null이면 해당 필드 null 반환")
  void from_nestedObjectsNull_returnsNull() {
    // given
    ConsultationSummary summary = new ConsultationSummary();
    summary.setConsultId(300L);

    // when
    ConsultationSummaryListResponse response =
        ConsultationSummaryListResponse.from(summary);

    // then
    assertThat(response.getConsultId()).isEqualTo(300L);

    assertThat(response.getCustomerName()).isNull();
    assertThat(response.getCustomerType()).isNull();

    assertThat(response.getCategoryLarge()).isNull();
    assertThat(response.getCategoryMedium()).isNull();
    assertThat(response.getCategorySmall()).isNull();

    assertThat(response.getAgentId()).isNull();
    assertThat(response.getAgentName()).isNull();
  }

  @Test
  @DisplayName("Customer만 존재하는 경우 부분 매핑 검증")
  void from_partialMapping_customerOnly() {
    // given
    ConsultationSummary summary = new ConsultationSummary();

    summary.setCustomer(
        ConsultationSummary.Customer.builder()
            .name("박고객")
            .type("법인")
            .build()
    );

    // when
    ConsultationSummaryListResponse response =
        ConsultationSummaryListResponse.from(summary);

    // then
    assertThat(response.getCustomerName()).isEqualTo("박고객");
    assertThat(response.getCustomerType()).isEqualTo("법인");

    assertThat(response.getCategoryLarge()).isNull();
    assertThat(response.getAgentId()).isNull();
  }
}