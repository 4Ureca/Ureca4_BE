package com.uplus.crm.domain.analysis.service;

import com.uplus.crm.domain.analysis.dto.AgentRankingResponse;
import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import com.uplus.crm.domain.analysis.dto.KeywordRankingResponse;
import com.uplus.crm.domain.analysis.dto.PerformanceSummaryResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

/**
 * DailyReportService — 기존 테스트(getCustomerRisk, compareCustomerRisk)에서
 * 커버하지 못한 메서드들의 테스트.
 */
@ExtendWith(MockitoExtension.class)
class DailyReportServiceExtTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks DailyReportService service;

    private static final LocalDate DATE = LocalDate.of(2025, 1, 15);

    // ==================== getDailyPerformanceSummary ====================

    @Test
    @DisplayName("일별 성과 요약 정상 조회")
    void getDailyPerformanceSummary_정상조회() {
        // given
        Document agent1 = createAgentDoc(1L, "김상담", 20, 5.5, 4.2, 3.8);
        Document agent2 = createAgentDoc(2L, "이상담", 30, 8.3, 4.5, 4.0);

        given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                .willReturn(List.of(agent1, agent2));

        // when
        Optional<PerformanceSummaryResponse> result = service.getDailyPerformanceSummary(DATE);

        // then
        assertThat(result).isPresent();
        PerformanceSummaryResponse r = result.get();
        assertThat(r.getTotalConsultCount()).isEqualTo(50); // 20+30
        assertThat(r.getAvgConsultCountPerAgent()).isEqualTo(25.0); // 50/2
    }

    @Test
    @DisplayName("스냅샷 없으면 empty 반환")
    void getDailyPerformanceSummary_스냅샷없음() {
        given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                .willReturn(List.of());

        assertThat(service.getDailyPerformanceSummary(DATE)).isEmpty();
    }

    // ==================== getDailyAgentRanking ====================

    @Test
    @DisplayName("상담사 랭킹 합산 점수 기준 정렬")
    void getDailyAgentRanking_정상조회() {
        // given: agent2가 더 높은 점수 → 1등이어야 함
        Document agent1 = createAgentDoc(1L, "김상담", 10, 10.0, 3.0, 2.5);
        Document agent2 = createAgentDoc(2L, "이상담", 30, 5.0, 4.5, 4.5);

        given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                .willReturn(List.of(agent1, agent2));

        // when
        Optional<AgentRankingResponse> result = service.getDailyAgentRanking(DATE);

        // then
        assertThat(result).isPresent();
        List<AgentRankingResponse.AgentPerformance> agents = result.get().getAgents();
        assertThat(agents).hasSize(2);
        assertThat(agents.get(0).getRank()).isEqualTo(1);
        assertThat(agents.get(0).getAgentId()).isEqualTo(2L); // 이상담이 1등
        assertThat(agents.get(1).getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("스냅샷 없으면 empty 반환")
    void getDailyAgentRanking_스냅샷없음() {
        given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                .willReturn(List.of());

        assertThat(service.getDailyAgentRanking(DATE)).isEmpty();
    }

    @Test
    @DisplayName("TOP 10까지만 반환")
    void getDailyAgentRanking_TOP10제한() {
        // given: 12명의 상담사
        List<Document> agents = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            agents.add(createAgentDoc(i, "상담사" + i, i * 5, 10.0 - i, 3.5, 3.5));
        }

        given(mongoTemplate.find(any(Query.class), eq(Document.class), eq("daily_agent_report_snapshot")))
                .willReturn(agents);

        // when
        Optional<AgentRankingResponse> result = service.getDailyAgentRanking(DATE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAgents()).hasSize(10);
        assertThat(result.get().getAgents().get(0).getRank()).isEqualTo(1);
        assertThat(result.get().getAgents().get(9).getRank()).isEqualTo(10);
    }

    // ==================== getDailyCustomerTypeKeywords ====================

    @Test
    @DisplayName("고객유형별 키워드 정상 조회")
    void getDailyCustomerTypeKeywords_정상조회() {
        // given: 배치(KeywordRankTasklet)가 저장하는 실제 구조와 동일하게 생성
        Document kw1 = new Document("keyword", "요금").append("count", 15);
        Document kw2 = new Document("keyword", "해지").append("count", 10);
        Document grade = new Document("customerType", "VIP")
                .append("keywords", List.of(kw1, kw2));
        Document keywordSummary = new Document("byCustomerType", List.of(grade));
        Document snapshot = new Document("startAt", DATE.atStartOfDay())
                .append("keywordSummary", keywordSummary);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(snapshot);

        // when
        Optional<KeywordAnalysisResponse> result = service.getDailyCustomerTypeKeywords(DATE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getByCustomerType()).hasSize(1);
        assertThat(result.get().getByCustomerType().get(0).getCustomerType()).isEqualTo("VIP");

        List<KeywordAnalysisResponse.CustomerKeywordCount> keywords =
                result.get().getByCustomerType().get(0).getKeywords();
        assertThat(keywords).hasSize(2);
        assertThat(keywords.get(0).getKeyword()).isEqualTo("요금");
        assertThat(keywords.get(0).getCount()).isEqualTo(15);
        assertThat(keywords.get(1).getKeyword()).isEqualTo("해지");
        assertThat(keywords.get(1).getCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("키워드 문서 없으면 empty 반환")
    void getDailyCustomerTypeKeywords_키워드문서없음() {
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(null);

        assertThat(service.getDailyCustomerTypeKeywords(DATE)).isEmpty();
    }

    // ==================== getKeywordRanking ====================

    @Test
    @DisplayName("키워드 랭킹 정상 조회")
    void getKeywordRanking_슬롯없음_정상() {
        // given: findSnapshot(startAt) → keywordSummary.topKeywords 조회
        Document topKw = new Document("keyword", "요금").append("count", 50)
                .append("changeRate", 10.5);
        Document keywordSummary = new Document("topKeywords", List.of(topKw));
        Document snapshot = new Document("startAt", DATE.atStartOfDay())
                .append("keywordSummary", keywordSummary);

        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(snapshot);

        // when
        Optional<KeywordRankingResponse> result = service.getKeywordRanking(DATE, null);

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("스냅샷 없으면 empty 반환")
    void getKeywordRanking_스냅샷없음() {
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("daily_report_snapshot")))
                .willReturn(null);

        assertThat(service.getKeywordRanking(DATE, null)).isEmpty();
    }

    // ==================== Helper ====================

    private Document createAgentDoc(long agentId, String name, int consultCount,
                                     double avgDuration, double satisfaction, double quality) {
        Document csAnalysis = new Document("satisfactionScore", satisfaction);
        Document qualityAnalysis = new Document("totalScore", quality);

        return new Document("agentId", agentId)
                .append("agentName", name)
                .append("consultCount", consultCount)
                .append("avgDurationMinutes", avgDuration)
                .append("customerSatisfactionAnalysis", csAnalysis)
                .append("qualityAnalysis", qualityAnalysis)
                .append("startAt", DATE.atStartOfDay());
    }
}
