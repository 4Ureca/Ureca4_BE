package com.uplus.crm.domain.analysis.service;

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
 * QualityAnalysisService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AgentQualityServiceTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks QualityAnalysisService qualityAnalysisService;

    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);
    private static final Long AGENT_ID = 1L;

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
