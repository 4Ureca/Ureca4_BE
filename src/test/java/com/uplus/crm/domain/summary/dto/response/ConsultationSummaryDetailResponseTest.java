package com.uplus.crm.domain.summary.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import org.mockito.Mockito;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.CustomerRepository.SubscribedProductProjection;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsultationSummaryDetailResponseTest {

    // ── 공통 픽스처 ────────────────────────────────────────────────────────

    private ConsultationResult makeResult(Long consultId) {
        return ConsultationResult.builder()
                .consultId(consultId)
                .empId(1)
                .customerId(10L)
                .channel("CALL")
                .categoryCode("M_FEE_01")
                .durationSec(300)
                .iamIssue("요금 문의")
                .iamAction("요금 안내")
                .iamMemo("특이사항 없음")
                .build();
    }

    private ConsultationSummary makeFullSummary(Long consultId) {
        ConsultationSummary s = new ConsultationSummary();
        s.setConsultId(consultId);
        s.setConsultedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        s.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 5));
        s.setDurationSec(300);
        s.setChannel("CALL");
        s.setCategory(ConsultationSummary.Category.builder()
                .code("M_FEE_01").large("요금").medium("할인").small("재약정").build());
        s.setAgent(ConsultationSummary.Agent.builder()._id(10L).name("홍길동").build());
        s.setCustomer(ConsultationSummary.Customer.builder()
                .grade("VIP").name("김고객").phone("01012345678")
                .type("개인").ageGroup("30대").satisfiedScore(4.5).build());
        s.setIam(ConsultationSummary.Iam.builder()
                .issue("요금 인상").action("요금 안내").memo("요금 문의")
                .matchRates(0.92).build());
        s.setSummary(ConsultationSummary.Summary.builder()
                .status("COMPLETED").content("AI 요약 내용").keywords(List.of("요금", "할인")).build());
        s.setRiskFlags(List.of("해지위험"));
        s.setCancellation(ConsultationSummary.Cancellation.builder()
                .intent(true).defenseAttempted(true).defenseSuccess(false)
                .defenseActions(List.of("요금 할인 제안")).complaintReasons("요금 부담").build());
        return s;
    }

    // ── 테스트 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("MongoDB + RDB 전체 데이터 병합 성공")
    void merge_fullData_success() {
        ConsultationResult result = makeResult(100L);
        ConsultationSummary summary = makeFullSummary(100L);
        ConsultationRawText rawText = Mockito.mock(ConsultationRawText.class);
        Mockito.when(rawText.getRawTextJson()).thenReturn("{\"script\":\"안녕하세요\"}");

        ConsultationSummaryDetailResponse response =
                ConsultationSummaryDetailResponse.merge(
                        result, summary, null, rawText, null, List.of(), null);

        assertThat(response.getConsultId()).isEqualTo(100L);
        assertThat(response.getChannel()).isEqualTo("CALL");

        // 카테고리 — MongoDB 우선
        assertThat(response.getCategory().getLarge()).isEqualTo("요금");
        assertThat(response.getCategory().getCode()).isEqualTo("M_FEE_01");

        // 상담사 — MongoDB 우선
        assertThat(response.getAgent().getId()).isEqualTo(10L);
        assertThat(response.getAgent().getName()).isEqualTo("홍길동");

        // 고객 — MongoDB 우선
        assertThat(response.getCustomer().getSatisfaction()).isEqualTo("4.5");
        assertThat(response.getCustomer().getGrade()).isEqualTo("VIP");

        // content
        assertThat(response.getContent().getAiSummary()).isEqualTo("AI 요약 내용");
        assertThat(response.getContent().getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getContent().getRawTextJson()).isEqualTo("{\"script\":\"안녕하세요\"}");

        // analysis
        assertThat(response.getAnalysis().getIamMatchRate()).isEqualTo(0.92);
        assertThat(response.getAnalysis().getRiskFlags()).containsExactly("해지위험");
        assertThat(response.getAnalysis().getCancellationIntent()).isTrue();
        assertThat(response.getAnalysis().getDefenseAttempted()).isTrue();
        assertThat(response.getAnalysis().getDefenseSuccess()).isFalse();
    }

    @Test
    @DisplayName("satisfiedScore null이면 '미작성'")
    void merge_satisfactionNull_returns미작성() {
        ConsultationResult result = makeResult(1L);
        ConsultationSummary summary = new ConsultationSummary();
        summary.setConsultId(1L);
        summary.setCustomer(ConsultationSummary.Customer.builder()
                .satisfiedScore(null).build());

        ConsultationSummaryDetailResponse response =
                ConsultationSummaryDetailResponse.merge(
                        result, summary, null, null, null, List.of(), null);

        assertThat(response.getCustomer().getSatisfaction()).isEqualTo("미작성");
    }

    @Test
    @DisplayName("MongoDB 없으면 RDB IAM 필드로 부분 응답")
    void merge_noMongoDB_usesRdbFallback() {
        ConsultationResult result = makeResult(2L);

        ConsultationSummaryDetailResponse response =
                ConsultationSummaryDetailResponse.merge(
                        result, null, null, null, null, List.of(), null);

        // RDB 기본 필드는 존재
        assertThat(response.getConsultId()).isEqualTo(2L);
        assertThat(response.getDurationSec()).isEqualTo(300);

        // RDB IAM fallback
        assertThat(response.getAnalysis().getIamIssue()).isEqualTo("요금 문의");
        assertThat(response.getAnalysis().getIamAction()).isEqualTo("요금 안내");

        // MongoDB 전용 필드는 null
        assertThat(response.getContent().getAiSummary()).isNull();
        assertThat(response.getAnalysis().getRiskFlags()).isNull();
        assertThat(response.getCustomer()).isNull();
    }

    @Test
    @DisplayName("RDB 카테고리 정책 fallback — MongoDB 카테고리 없을 때")
    void merge_categoryFallback_usesRdbCategory() {
        ConsultationResult result = makeResult(3L);
        ConsultationSummary summaryNoCategory = new ConsultationSummary();
        summaryNoCategory.setConsultId(3L);
        // category 설정 안 함

        ConsultationSummaryDetailResponse response =
                ConsultationSummaryDetailResponse.merge(
                        result, summaryNoCategory, null, null, null, List.of(), null);

        // MongoDB category null이면 RDB categoryCode 반환
        assertThat(response.getCategory().getCode()).isEqualTo("M_FEE_01");
    }

    @Test
    @DisplayName("현재 가입 상품 목록 매핑")
    void merge_activeSubscriptions_mapped() {
        ConsultationResult result = makeResult(4L);

        SubscribedProductProjection home = new SubscribedProductProjection() {
            public String getProductType() { return "HOME"; }
            public String getProductCode() { return "GIGA_SLIM"; }
            public String getProductName() { return "기가슬림"; }
            public String getCategory() { return "인터넷"; }
        };

        ConsultationSummaryDetailResponse response =
                ConsultationSummaryDetailResponse.merge(
                        result, null, null, null, null, List.of(home), null);

        assertThat(response.getActiveSubscriptions()).hasSize(1);
        assertThat(response.getActiveSubscriptions().get(0).getProductType()).isEqualTo("HOME");
        assertThat(response.getActiveSubscriptions().get(0).getProductName()).isEqualTo("기가슬림");
    }
}
