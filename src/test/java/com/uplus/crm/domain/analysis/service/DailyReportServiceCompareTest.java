package com.uplus.crm.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.uplus.crm.domain.analysis.dto.CustomerRiskCompareResponse;
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

@ExtendWith(MockitoExtension.class)
class DailyReportServiceCompareTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DailyReportService service;

    private Document createRiskDoc(LocalDate date, int fraud, int malicious, int policy,
                                    int excessive, int repeated, int phishing, int churn) {
        Document risk = new Document()
                .append("fraudSuspect", fraud)
                .append("maliciousComplaint", malicious)
                .append("policyAbuse", policy)
                .append("excessiveCompensation", excessive)
                .append("repeatedComplaint", repeated)
                .append("phishingVictim", phishing)
                .append("churnRisk", churn);
        return new Document("startAt", date.atStartOfDay())
                .append("customerRiskAnalysis", risk);
    }

    @Test
    @DisplayName("compare - two snapshots exist, fraud surges detected")
    void compareCustomerRisk_success() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 15);
        LocalDate compareDate = LocalDate.of(2025, 1, 10);

        Document baseDoc = createRiskDoc(baseDate, 10, 6, 1, 4, 10, 1, 3);
        Document compareDoc = createRiskDoc(compareDate, 2, 3, 1, 2, 5, 0, 1);

        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(baseDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(baseDoc);
        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(compareDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(compareDoc);

        // when
        CustomerRiskCompareResponse result = service.compareCustomerRisk(baseDate, compareDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBaseDate()).isEqualTo(baseDate);
        assertThat(result.getCompareDate()).isEqualTo(compareDate);

        // base snapshot
        assertThat(result.getBase().getFraudSuspect()).isEqualTo(10);
        assertThat(result.getBase().getTotalRiskCount()).isEqualTo(35);

        // compare snapshot
        assertThat(result.getCompare().getFraudSuspect()).isEqualTo(2);
        assertThat(result.getCompare().getTotalRiskCount()).isEqualTo(12);

        // changes
        assertThat(result.getChanges().get("fraudSuspect").getDiff()).isEqualTo(8);
        assertThat(result.getChanges().get("fraudSuspect").getChangeRate()).isEqualTo(400.0);
        assertThat(result.getChanges().get("totalRiskCount").getDiff()).isEqualTo(23);

        // surge
        assertThat(result.isSurgeDetected()).isTrue();
        assertThat(result.getSurgeTypes()).contains("FRAUD");
    }

    @Test
    @DisplayName("compare - base snapshot not found, returns null")
    void compareCustomerRisk_baseNotFound_returnsNull() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 15);
        LocalDate compareDate = LocalDate.of(2025, 1, 10);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(null);

        // when
        CustomerRiskCompareResponse result = service.compareCustomerRisk(baseDate, compareDate);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("compare - same values, zero change rate, no surge")
    void compareCustomerRisk_sameValues_zeroChange() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 15);
        LocalDate compareDate = LocalDate.of(2025, 1, 14);

        Document baseDoc = createRiskDoc(baseDate, 5, 3, 0, 0, 2, 0, 1);
        Document compareDoc = createRiskDoc(compareDate, 5, 3, 0, 0, 2, 0, 1);

        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(baseDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(baseDoc);
        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(compareDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(compareDoc);

        // when
        CustomerRiskCompareResponse result = service.compareCustomerRisk(baseDate, compareDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChanges().get("totalRiskCount").getDiff()).isEqualTo(0);
        assertThat(result.getChanges().get("totalRiskCount").getChangeRate()).isEqualTo(0.0);
        assertThat(result.isSurgeDetected()).isFalse();
        assertThat(result.getSurgeTypes()).isEmpty();
    }

    @Test
    @DisplayName("compare - decrease scenario, negative diff")
    void compareCustomerRisk_decrease_negativeDiff() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 15);
        LocalDate compareDate = LocalDate.of(2025, 1, 10);

        Document baseDoc = createRiskDoc(baseDate, 2, 1, 0, 0, 1, 0, 0);
        Document compareDoc = createRiskDoc(compareDate, 10, 6, 2, 3, 8, 1, 3);

        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(baseDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(baseDoc);
        given(mongoTemplate.findOne(
                argThat(q -> q.getQueryObject().get("startAt").equals(compareDate.atStartOfDay())),
                eq(Document.class), eq("daily_report_snapshot")
        )).willReturn(compareDoc);

        // when
        CustomerRiskCompareResponse result = service.compareCustomerRisk(baseDate, compareDate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getChanges().get("fraudSuspect").getDiff()).isEqualTo(-8);
        assertThat(result.getChanges().get("totalRiskCount").getDiff()).isLessThan(0);
        assertThat(result.isSurgeDetected()).isFalse();
    }
}