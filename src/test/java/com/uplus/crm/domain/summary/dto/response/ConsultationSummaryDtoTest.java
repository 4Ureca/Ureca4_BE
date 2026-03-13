package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationSummaryDtoTest {

  @Test
  @DisplayName("from - 모든 필드를 정상적으로 매핑한다")
  void from_mapsAllFields() {

    ConsultationSummary.Customer customer =
        ConsultationSummary.Customer.builder()
            ._id(1L)
            .phone("010-1234")
            .name("김고객")
            .grade("VIP")
            .build();

    ConsultationSummary.Agent agent =
        ConsultationSummary.Agent.builder()
            ._id(7L)
            .name("홍상담")
            .build();

    ConsultationSummary.Category category =
        ConsultationSummary.Category.builder()
            .large("요금")
            .medium("청구")
            .small("문의")
            .build();

    ConsultationSummary.Summary summary =
        ConsultationSummary.Summary.builder()
            .content("상담 요약 내용")
            .build();

    ConsultationSummary.Cancellation cancellation =
        ConsultationSummary.Cancellation.builder()
            .intent(true)
            .defenseSuccess(true)
            .build();

    ConsultationSummary.RiskFlag risk =
        ConsultationSummary.RiskFlag.builder()
            .riskType("RT1")
            .riskLevel("RL1")
            .build();

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(100L)
            .consultedAt(LocalDateTime.of(2026, 3, 10, 0, 0))
            .channel("CALL")
            .customer(customer)
            .agent(agent)
            .category(category)
            .summary(summary)
            .cancellation(cancellation)
            .riskFlags(List.of(risk))
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getConsultId()).isEqualTo(100L);
    assertThat(dto.getChannel()).isEqualTo("CALL");
    assertThat(dto.getCustomerName()).isEqualTo("김고객");
    assertThat(dto.getPhone()).isEqualTo("010-1234");
    assertThat(dto.getGrade()).isEqualTo("VIP");
    assertThat(dto.getAgentName()).isEqualTo("홍상담");
    assertThat(dto.getCategoryLarge()).isEqualTo("요금");
    assertThat(dto.getCategoryMedium()).isEqualTo("청구");
    assertThat(dto.getCategorySmall()).isEqualTo("문의");
    assertThat(dto.getSummaryContent()).isEqualTo("상담 요약 내용");
    assertThat(dto.getIntent()).isTrue();
    assertThat(dto.getDefenseSuccess()).isTrue();

    assertThat(dto.getRiskFlags()).hasSize(1);
    assertThat(dto.getRiskFlags().get(0).getRiskType()).isEqualTo("RT1");
    assertThat(dto.getRiskFlags().get(0).getRiskLevel()).isEqualTo("RL1");
  }

  @Test
  @DisplayName("from - UTC 시간을 KST로 변환한다")
  void from_convertsUtcToKst() {

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(1L)
            .consultedAt(LocalDateTime.of(2026, 3, 10, 0, 0))
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getConsultedAt())
        .isEqualTo(LocalDateTime.of(2026, 3, 10, 9, 0));
  }

  @Test
  @DisplayName("from - summary가 null이면 빈 문자열 반환")
  void from_summaryNull_returnsEmptyString() {

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(1L)
            .summary(null)
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getSummaryContent()).isEqualTo("");
  }

  @Test
  @DisplayName("from - riskFlags가 null이면 빈 리스트 반환")
  void from_riskFlagsNull_returnsEmptyList() {

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(1L)
            .riskFlags(null)
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getRiskFlags()).isEmpty();
  }

  @Test
  @DisplayName("from - customer/agent/category가 null이어도 예외 발생하지 않는다")
  void from_handlesNullNestedObjects() {

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(1L)
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getCustomerName()).isNull();
    assertThat(dto.getAgentName()).isNull();
    assertThat(dto.getCategoryLarge()).isNull();
  }

  @Test
  @DisplayName("from - cancellation이 null이면 intent와 defenseSuccess는 null")
  void from_cancellationNull_returnsNullFlags() {

    ConsultationSummary doc =
        ConsultationSummary.builder()
            .consultId(1L)
            .cancellation(null)
            .build();

    ConsultationSummaryDto dto = ConsultationSummaryDto.from(doc);

    assertThat(dto.getIntent()).isNull();
    assertThat(dto.getDefenseSuccess()).isNull();
  }
}