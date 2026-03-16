package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.ChurnDefenseResponse;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
import com.uplus.crm.domain.common.repository.AnalysisCodeRepository;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * MonthlyReportService — 기존 키워드 테스트 외
 * getMonthlyCustomerRisk, getMonthlyChurnDefenseAnalysis 테스트.
 */
@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceExtTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks MonthlyReportService service;

    @Mock
    private AnalysisCodeRepository analysisCodeRepository;

    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);

    // ==================== getMonthlyCustomerRisk ====================

    @Test
    @DisplayName("월별 고객 위험 정상 조회")
    void getMonthlyCustomerRisk_정상조회() {
        // given
        Document risk = new Document()
                .append("fraudSuspect", 5).append("maliciousComplaint", 3)
                .append("policyAbuse", 2).append("excessiveCompensation", 1)
                .append("repeatedComplaint", 8).append("phishingVictim", 0)
                .append("churnRisk", 4)
                .append("totalRiskCount", 23);

        Document snapshot = createMonthlySnapshot(risk);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        // when
        CustomerRiskResponse result = service.getMonthlyCustomerRisk(DATE);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFraudSuspect()).isEqualTo(5);
        assertThat(result.getChurnRisk()).isEqualTo(4);
        assertThat(result.getTotalRiskCount()).isEqualTo(23);
        assertThat(result.getSurgeAlerts()).isNull(); // 월별은 급증 경고 없음
    }

    @Test
    @DisplayName("스냅샷 없으면 null 반환")
    void getMonthlyCustomerRisk_스냅샷없음() {
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(null);

        assertThat(service.getMonthlyCustomerRisk(DATE)).isNull();
    }

    @Test
    @DisplayName("필드 없으면 기본값 반환")
    void getMonthlyCustomerRisk_필드없음() {
        Document snapshot = new Document("startAt", new Date()).append("endAt", new Date());

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        assertThat(service.getMonthlyCustomerRisk(DATE)).isNull();
    }

    // ==================== getMonthlyChurnDefenseAnalysis ====================

    @Test
    @DisplayName("해지방어 분석 정상 조회")
    void getMonthlyChurnDefenseAnalysis_정상조회() {
        // given
        Document reason = new Document("reason", "요금 불만")
                .append("attempts", 30).append("successCount", 18)
                .append("successRate", 60.0).append("avgDurationSec", 480);
        Document customerType = new Document("type", "30대 남성")
                .append("mainComplaintReason", "경쟁사 이동")
                .append("attempts", 15).append("successRate", 46.7);
        Document action = new Document("action", "요금할인")
                .append("attempts", 25).append("successRate", 72.0);

        Document defense = new Document()
                .append("totalAttempts", 85).append("successCount", 52)
                .append("successRate", 61.2).append("avgDurationSec", 520)
                .append("complaintReasons", List.of(reason))
                .append("byCustomerType", List.of(customerType))
                .append("byAction", List.of(action));

        Document snapshot = new Document()
                .append("startAt", new Date()).append("endAt", new Date())
                .append("churnDefenseAnalysis", defense);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        given(analysisCodeRepository.findAll()).willReturn(List.of());

        // when
        ChurnDefenseResponse result = service.getMonthlyChurnDefenseAnalysis(DATE);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAttempts()).isEqualTo(85);
        assertThat(result.getSuccessRate()).isEqualTo(61.2);
        assertThat(result.getComplaintReasons()).hasSize(1);
        assertThat(result.getComplaintReasons().get(0).getReason()).isEqualTo("요금 불만");
        assertThat(result.getByCustomerType()).hasSize(1);
        assertThat(result.getByAction()).hasSize(1);
        assertThat(result.getByAction().get(0).getAction()).isEqualTo("요금할인");
    }

    @Test
    @DisplayName("스냅샷 없으면 null 반환")
    void getMonthlyChurnDefenseAnalysis_스냅샷없음() {
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(null);

        assertThat(service.getMonthlyChurnDefenseAnalysis(DATE)).isNull();
    }

    @Test
    @DisplayName("하위 리스트 null이면 빈 리스트 반환")
    void getMonthlyChurnDefenseAnalysis_하위리스트null() {
        // given: churnDefenseAnalysis는 있지만 하위 리스트가 null
        Document defense = new Document()
                .append("totalAttempts", 10).append("successCount", 5)
                .append("successRate", 50.0).append("avgDurationSec", 300);
        // complaintReasons, byCustomerType, byAction 필드 없음

        Document snapshot = new Document()
                .append("startAt", new Date()).append("endAt", new Date())
                .append("churnDefenseAnalysis", defense);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        // when
        ChurnDefenseResponse result = service.getMonthlyChurnDefenseAnalysis(DATE);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalAttempts()).isEqualTo(10);
        assertThat(result.getComplaintReasons()).isEmpty();
        assertThat(result.getByCustomerType()).isEmpty();
        assertThat(result.getByAction()).isEmpty();
    }

    // ==================== Helper ====================

    private Document createMonthlySnapshot(Document riskAnalysis) {
        return new Document()
                .append("startAt", new Date())
                .append("endAt", new Date())
                .append("customerRiskAnalysis", riskAnalysis);
    }
}
