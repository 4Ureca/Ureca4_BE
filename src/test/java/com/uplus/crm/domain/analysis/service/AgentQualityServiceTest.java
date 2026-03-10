package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.AgentReportResponse;
import com.uplus.crm.domain.analysis.dto.QualityAnalysisResponse;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * AgentReportService + QualityAnalysisService 통합 테스트
 */
@ExtendWith(MockitoExtension.class)
class AgentQualityServiceTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks AgentReportService agentReportService;
    @InjectMocks QualityAnalysisService qualityAnalysisService;

    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);
    private static final Long AGENT_ID = 1L;

    // ==================== AgentReportService ====================

    @Nested
    @DisplayName("AgentReportService")
    class AgentReportTests {

        @Test
        @DisplayName("일별 리포트 정상 조회")
        void getDailyReport_정상조회() {
            // given
            Document doc = createAgentReportDoc();

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                    .willReturn(doc);

            // when
            Optional<AgentReportResponse> result = agentReportService.getDailyReport(AGENT_ID, DATE);

            // then
            assertThat(result).isPresent();
            AgentReportResponse r = result.get();
            assertThat(r.getAgentId()).isEqualTo(AGENT_ID);
            assertThat(r.getConsultCount()).isEqualTo(25);
            assertThat(r.getAvgDurationMinutes()).isEqualTo(7.5);
            assertThat(r.getCategoryRanking()).hasSize(1);
            assertThat(r.getQualityAnalysis()).isNotNull();
            assertThat(r.getQualityAnalysis().getTotalScore()).isEqualTo(4.2);
        }

        @Test
        @DisplayName("스냅샷 없으면 empty 반환")
        void getDailyReport_스냅샷없음() {
            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                    .willReturn(null);

            assertThat(agentReportService.getDailyReport(AGENT_ID, DATE)).isEmpty();
        }

        @Test
        @DisplayName("주별 리포트 범위 쿼리 조회")
        void getWeeklyReport_정상조회() {
            Document doc = createAgentReportDoc();

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("weekly_agent_report_snapshot")))
                    .willReturn(doc);

            Optional<AgentReportResponse> result = agentReportService.getWeeklyReport(AGENT_ID, DATE);

            assertThat(result).isPresent();
            assertThat(result.get().getAgentId()).isEqualTo(AGENT_ID);
        }
    }

    // ==================== QualityAnalysisService ====================

    @Nested
    @DisplayName("QualityAnalysisService")
    class QualityAnalysisTests {

        @Test
        @DisplayName("특정 상담사 품질 분석 조회 성공")
        void getDailyByAgent_정상조회() {
            Document doc = createQualityDoc();

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                    .willReturn(doc);

            Optional<QualityAnalysisResponse> result = qualityAnalysisService.getDailyByAgent(AGENT_ID, DATE);

            assertThat(result).isPresent();
            QualityAnalysisResponse r = result.get();
            assertThat(r.getAgentId()).isEqualTo(AGENT_ID);
            assertThat(r.getTotalScore()).isEqualTo(4.2);
            assertThat(r.getApologyRate()).isEqualTo(85.0);
        }

        @Test
        @DisplayName("qualityAnalysis 없으면 empty 반환")
        void getDailyByAgent_qualityAnalysis없음() {
            Document doc = new Document("agentId", AGENT_ID)
                    .append("startAt", DATE.atStartOfDay())
                    .append("consultCount", 10);
            // qualityAnalysis 필드 없음

            given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                    .willReturn(doc);

            assertThat(qualityAnalysisService.getDailyByAgent(AGENT_ID, DATE)).isEmpty();
        }

        @Test
        @DisplayName("전체 상담사 품질 분석 조회")
        void getDailyAll_정상조회() {
            Document doc1 = createQualityDoc();
            Document doc2 = createQualityDoc(2L, 3.8);

            given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                    .willReturn(List.of(doc1, doc2));

            List<QualityAnalysisResponse> result = qualityAnalysisService.getDailyAll(DATE);

            assertThat(result).hasSize(2);
        }
    }

    // ==================== Helper ====================

    private Document createAgentReportDoc() {
        Document category = new Document("rank", 1).append("code", "CAT001")
                .append("large", "요금").append("medium", "청구").append("count", 10);

        Document qualityAnalysis = new Document()
                .append("empathyCount", 15L).append("avgEmpathyPerConsult", 0.6)
                .append("apologyRate", 85.0).append("closingRate", 92.0)
                .append("courtesyRate", 88.0).append("promptnessRate", 90.0)
                .append("accuracyRate", 87.0).append("waitingGuideRate", 75.0)
                .append("totalScore", 4.2).append("analyzedCount", 20);

        Document csAnalysis = new Document("satisfactionScore", 4.5);

        return new Document("agentId", AGENT_ID)
                .append("agentName", "김상담")
                .append("startAt", DATE.atStartOfDay())
                .append("endAt", DATE.atTime(23, 59, 59))
                .append("consultCount", 25)
                .append("avgDurationMinutes", 7.5)
                .append("customerSatisfactionAnalysis", csAnalysis)
                .append("categoryRanking", List.of(category))
                .append("qualityAnalysis", qualityAnalysis);
    }

    private Document createQualityDoc() {
        return createQualityDoc(AGENT_ID, 4.2);
    }

    private Document createQualityDoc(Long agentId, double totalScore) {
        Document qualityAnalysis = new Document()
                .append("empathyCount", 15L).append("avgEmpathyPerConsult", 0.6)
                .append("apologyRate", 85.0).append("closingRate", 92.0)
                .append("courtesyRate", 88.0).append("promptnessRate", 90.0)
                .append("accuracyRate", 87.0).append("waitingGuideRate", 75.0)
                .append("totalScore", totalScore).append("analyzedCount", 20);

        return new Document("agentId", agentId)
                .append("startAt", DATE.atStartOfDay())
                .append("endAt", DATE.atTime(23, 59, 59))
                .append("consultCount", 25)
                .append("qualityAnalysis", qualityAnalysis);
    }
}
