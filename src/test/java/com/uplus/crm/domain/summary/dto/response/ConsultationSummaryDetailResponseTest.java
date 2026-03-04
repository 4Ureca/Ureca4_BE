package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationSummaryDetailResponseTest {

  @Test
  @DisplayName("정상 매핑 테스트 - 모든 필드 존재")
  void from_fullMapping_success() {
    // given
    ConsultationSummary summary = new ConsultationSummary();

    summary.setConsultId(100L);
    summary.setConsultedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
    summary.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 5));
    summary.setDurationSec(300);
    summary.setChannel("CALL");

    summary.setCategory(
        ConsultationSummary.Category.builder()
            .large("요금")
            .medium("할인")
            .small("재약정")
            .build()
    );

    summary.setAgent(
        ConsultationSummary.Agent.builder()
            ._id(10L)
            .name("홍길동")
            .build()
    );

    summary.setCustomer(
        ConsultationSummary.Customer.builder()
            .grade("VIP")
            .name("김고객")
            .phone("01012345678")
            .type("개인")
            .ageGroup("30대")
            .satisfiedScore(4.5)
            .build()
    );

    summary.setIam(
        ConsultationSummary.Iam.builder()
            .memo("요금 문의")
            .action("요금 안내")
            .issue("요금 인상")
            .matchRates(0.92)
            .build()
    );

    summary.setCancellation(
        ConsultationSummary.Cancellation.builder()
            .intent(true)
            .defenseAttempted(true)
            .defenseSuccess(false)
            .defenseActions(List.of("요금 할인 제안"))
            .complaintReasons("요금 부담")
            .build()
    );

    summary.setResultProducts(
        List.of(
            ConsultationSummary.ResultProducts.builder()
                .subscribed(List.of("인터넷"))
                .canceled(List.of("IPTV"))
                .conversion(List.of())
                .recommitment(List.of("재약정 1년"))
                .changeType("UPGRADE")
                .build()
        )
    );

    // when
    ConsultationSummaryDetailResponse response =
        ConsultationSummaryDetailResponse.from(summary);

    // then
    assertThat(response.getConsultId()).isEqualTo(100L);
    assertThat(response.getChannel()).isEqualTo("CALL");

    assertThat(response.getCategory().getLarge()).isEqualTo("요금");
    assertThat(response.getAgent().getId()).isEqualTo(10L);
    assertThat(response.getCustomer().getSatisfaction()).isEqualTo("4.5");

    assertThat(response.getIam().getMatchRate()).isEqualTo(0.92);

    assertThat(response.getCancellation().getIntent()).isTrue();
    assertThat(response.getProducts()).hasSize(1);
    assertThat(response.getProducts().get(0).getChangeType())
        .isEqualTo("UPGRADE");
  }

  @Test
  @DisplayName("satisfiedScore null이면 '미작성'")
  void from_satisfactionNull_returns미작성() {
    // given
    ConsultationSummary summary = new ConsultationSummary();
    summary.setCustomer(
        ConsultationSummary.Customer.builder()
            .satisfiedScore(null)
            .build()
    );

    // when
    ConsultationSummaryDetailResponse response =
        ConsultationSummaryDetailResponse.from(summary);

    // then
    assertThat(response.getCustomer().getSatisfaction())
        .isEqualTo("미작성");
  }

  @Test
  @DisplayName("하위 객체 null이면 해당 필드 null 반환")
  void from_nestedObjectsNull_returnsNull() {
    // given
    ConsultationSummary summary = new ConsultationSummary();

    // when
    ConsultationSummaryDetailResponse response =
        ConsultationSummaryDetailResponse.from(summary);

    // then
    assertThat(response.getCategory()).isNull();
    assertThat(response.getAgent()).isNull();
    assertThat(response.getCustomer()).isNull();
    assertThat(response.getIam()).isNull();
    assertThat(response.getCancellation()).isNull();
    assertThat(response.getProducts()).isNull();
  }
}