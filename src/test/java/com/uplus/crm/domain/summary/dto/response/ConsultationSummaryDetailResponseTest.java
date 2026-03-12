package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsultationSummaryDetailResponseTest {

  @Test
  void resultProducts_null_case() {

    ConsultationSummary summary = new ConsultationSummary();
    summary.setResultProducts(null);

    ConsultationSummaryDetailResponse response =
        ConsultationSummaryDetailResponse.from(summary, Map.of());

    assertThat(response.getResultProducts()).isNull();
  }

  @Test
  void change_case() {

    ConsultationSummary.ResultProducts.Conversion conversion =
        new ConsultationSummary.ResultProducts.Conversion("NEW-PROD","OLD-PROD");

    ConsultationSummary.ResultProducts rp = new ConsultationSummary.ResultProducts();
    rp.setChangeType("CHANGE");
    rp.setConversion(List.of(conversion));

    ConsultationSummary summary = new ConsultationSummary();
    summary.setResultProducts(List.of(rp));

    Map<String,String> productMap =
        Map.of(
            "NEW-PROD","신규상품",
            "OLD-PROD","기존상품"
        );

    var response = ConsultationSummaryDetailResponse.from(summary, productMap);

    var action = response.getResultProducts().get(0).getProducts().get(0);

    assertThat(action.getBefore().getCode()).isEqualTo("OLD-PROD");
    assertThat(action.getAfter().getCode()).isEqualTo("NEW-PROD");
  }

  @Test
  void subscribed_case() {

    ConsultationSummary.ResultProducts rp = new ConsultationSummary.ResultProducts();
    rp.setChangeType("NEW");
    rp.setSubscribed(List.of("MOB-5G"));

    ConsultationSummary summary = new ConsultationSummary();
    summary.setResultProducts(List.of(rp));

    Map<String,String> productMap =
        Map.of("MOB-5G","5G 요금제");

    var response = ConsultationSummaryDetailResponse.from(summary, productMap);

    var action = response.getResultProducts().get(0).getProducts().get(0);

    assertThat(action.getBefore()).isNull();
    assertThat(action.getAfter().getCode()).isEqualTo("MOB-5G");
  }

  @Test
  void canceled_case() {

    ConsultationSummary.ResultProducts rp = new ConsultationSummary.ResultProducts();
    rp.setChangeType("CANCEL");
    rp.setCanceled(List.of("TV-STD"));

    ConsultationSummary summary = new ConsultationSummary();
    summary.setResultProducts(List.of(rp));

    Map<String,String> productMap =
        Map.of("TV-STD","TV 스탠다드");

    var response = ConsultationSummaryDetailResponse.from(summary, productMap);

    var action = response.getResultProducts().get(0).getProducts().get(0);

    assertThat(action.getBefore().getCode()).isEqualTo("TV-STD");
    assertThat(action.getAfter()).isNull();
  }

  @Test
  void recommitment_case() {

    ConsultationSummary.ResultProducts rp = new ConsultationSummary.ResultProducts();
    rp.setChangeType("RENEW");
    rp.setRecommitment(List.of("SH-KIDS"));

    ConsultationSummary summary = new ConsultationSummary();
    summary.setResultProducts(List.of(rp));

    Map<String,String> productMap =
        Map.of("SH-KIDS","키즈 패키지");

    var response = ConsultationSummaryDetailResponse.from(summary, productMap);

    var action = response.getResultProducts().get(0).getProducts().get(0);

    assertThat(action.getBefore()).isNull();
    assertThat(action.getAfter().getCode()).isEqualTo("SH-KIDS");
  }

  @Test
  void productInfo_null_code() {

    ConsultationSummaryDetailResponse.ProductInfo info =
        ConsultationSummaryDetailResponse
            .ProductInfo.builder()
            .code(null)
            .name(null)
            .build();

    assertThat(info.getCode()).isNull();
  }

  @Test
  void convertUtcToKst_null() {

    ConsultationSummary summary = new ConsultationSummary();
    summary.setConsultedAt(null);
    summary.setCreatedAt(null);

    var response =
        ConsultationSummaryDetailResponse.from(summary, Map.of());

    assertThat(response.getConsultedAt()).isNull();
    assertThat(response.getCreatedAt()).isNull();
  }

  @Test
  void basic_mapping() {

    ConsultationSummary summary = new ConsultationSummary();

    summary.setId("mongoId");
    summary.setConsultId(1L);
    summary.setChannel("CALL");
    summary.setDurationSec(300);
    summary.setConsultedAt(LocalDateTime.of(2026,3,1,10,0));
    summary.setCreatedAt(LocalDateTime.of(2026,3,1,11,0));

    var response =
        ConsultationSummaryDetailResponse.from(summary, Map.of());

    assertThat(response.getId()).isEqualTo("mongoId");
    assertThat(response.getConsultId()).isEqualTo(1L);
    assertThat(response.getChannel()).isEqualTo("CALL");
    assertThat(response.getDurationSec()).isEqualTo(300);
  }
}