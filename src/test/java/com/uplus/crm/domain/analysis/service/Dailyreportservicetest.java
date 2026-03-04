package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.analysis.dto.CustomerRiskResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DailyReportServiceTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks DailyReportService service;

    @Test
    @DisplayName("정상 조회 - customerRiskAnalysis 필드 매핑")
    void getCustomerRisk_success() {
        // given
        Document risk = new Document()
                .append("fraudSuspect", 2).append("maliciousComplaint", 6)
                .append("policyAbuse", 1).append("excessiveCompensation", 4)
                .append("repeatedComplaint", 10).append("phishingVictim", 1)
                .append("churnRisk", 3);
        Document snapshot = new Document("customerRiskAnalysis", risk);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(snapshot).willReturn(null);

        // when
        CustomerRiskResponse result = service.getCustomerRisk(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFraudSuspect()).isEqualTo(2);
        assertThat(result.getMaliciousComplaint()).isEqualTo(6);
        assertThat(result.getTotalRiskCount()).isEqualTo(27);
        assertThat(result.getSurgeAlerts()).isNull();
    }

    @Test
    @DisplayName("스냅샷 없음 - null 반환")
    void getCustomerRisk_noSnapshot() {
        // given
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(null);

        // when & then
        assertThat(service.getCustomerRisk(LocalDate.of(2025, 1, 15))).isNull();
    }

    @Test
    @DisplayName("customerRiskAnalysis 필드 누락 - BusinessException 발생")
    void getCustomerRisk_missingField_throwsException() {
        // given
        Document snapshotWithoutRisk = new Document("someOtherField", "value");

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(snapshotWithoutRisk);

        // when & then
        assertThatThrownBy(() -> service.getCustomerRisk(LocalDate.of(2025, 1, 15)))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.DATA_INTEGRITY_ERROR);
                });
    }

    @Test
    @DisplayName("급증 경고 - 전일 대비 FRAUD 5배 급증")
    void getCustomerRisk_surgeDetected() {
        // given
        Document todayRisk = new Document()
                .append("fraudSuspect", 10).append("maliciousComplaint", 2)
                .append("policyAbuse", 0).append("excessiveCompensation", 0)
                .append("repeatedComplaint", 0).append("phishingVictim", 0)
                .append("churnRisk", 0);
        Document prevRisk = new Document()
                .append("fraudSuspect", 2).append("maliciousComplaint", 2)
                .append("policyAbuse", 0).append("excessiveCompensation", 0)
                .append("repeatedComplaint", 0).append("phishingVictim", 0)
                .append("churnRisk", 0);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(new Document("customerRiskAnalysis", todayRisk))
                .willReturn(new Document("customerRiskAnalysis", prevRisk));

        // when
        CustomerRiskResponse result = service.getCustomerRisk(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result.getSurgeAlerts()).isNotNull();
        assertThat(result.getSurgeAlerts().isSurgeDetected()).isTrue();
        assertThat(result.getSurgeAlerts().getSurgeTypes()).contains("FRAUD");
        assertThat(result.getSurgeAlerts().getSurgeTypes()).doesNotContain("ABUSE");
    }
}