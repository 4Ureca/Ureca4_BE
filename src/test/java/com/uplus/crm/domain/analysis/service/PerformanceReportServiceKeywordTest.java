package com.uplus.crm.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.uplus.crm.domain.analysis.dto.KeywordAnalysisResponse;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceReportService 키워드 분석 테스트")
class PerformanceReportServiceKeywordTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks PerformanceReportService service;

    // ==================== 헬퍼 ====================

    private Document createSnapshotWithKeywordSummary() {
        Document topKw = new Document("keyword", "해지")
                .append("count", 50)
                .append("rank", 1)
                .append("changeRate", 25.5);

        Document longTerm = new Document("keyword", "해지")
                .append("count", 320)
                .append("rank", 1)
                .append("appearanceDays", 20)
                .append("totalDays", 28);

        Document ct = new Document("customerType", "VIP")
                .append("keywords", List.of("해지", "요금제", "번호이동"));

        Document keywordSummary = new Document()
                .append("topKeywords", List.of(topKw))
                .append("longTermTopKeywords", List.of(longTerm))
                .append("byCustomerType", List.of(ct));

        return new Document()
                .append("startAt", LocalDateTime.of(2025, 1, 8, 0, 0, 0))
                .append("endAt", LocalDateTime.of(2025, 1, 15, 23, 59, 59))
                .append("keywordSummary", keywordSummary);
    }

    // ==================== 주간 키워드 분석 ====================

    @Test
    @DisplayName("주간 스냅샷 존재 → Optional.of(response)")
    void getWeeklyKeywordAnalysis_정상조회() {
        // given
        Document snapshot = createSnapshotWithKeywordSummary();
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("weekly_report_snapshot")))
                .willReturn(snapshot);

        // when
        Optional<KeywordAnalysisResponse> result =
                service.getWeeklyKeywordAnalysis(LocalDate.of(2025, 1, 10));

        // then
        assertThat(result).isPresent();
        KeywordAnalysisResponse response = result.get();
        assertThat(response.getStartDate()).isEqualTo("2025-01-08");
        assertThat(response.getEndDate()).isEqualTo("2025-01-15");
        assertThat(response.getTopKeywords()).hasSize(1);
        assertThat(response.getTopKeywords().get(0).getKeyword()).isEqualTo("해지");
        assertThat(response.getTopKeywords().get(0).getCount()).isEqualTo(50);
        assertThat(response.getLongTermTopKeywords()).hasSize(1);
        assertThat(response.getByCustomerType()).hasSize(1);
    }

    @Test
    @DisplayName("주간 스냅샷 없음 → Optional.empty()")
    void getWeeklyKeywordAnalysis_스냅샷없음() {
        // given
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("weekly_report_snapshot")))
                .willReturn(null);

        // when
        Optional<KeywordAnalysisResponse> result =
                service.getWeeklyKeywordAnalysis(LocalDate.of(2025, 1, 10));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("주간 스냅샷 존재하나 keywordSummary 없음 → Optional.empty()")
    void getWeeklyKeywordAnalysis_keywordSummary없음() {
        // given — keywordSummary 필드 없는 스냅샷
        Document snapshot = new Document("someField", "value");
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("weekly_report_snapshot")))
                .willReturn(snapshot);

        // when
        Optional<KeywordAnalysisResponse> result =
                service.getWeeklyKeywordAnalysis(LocalDate.of(2025, 1, 10));

        // then
        assertThat(result).isEmpty();
    }

    // ==================== 월간 키워드 분석 ====================

    @Test
    @DisplayName("월간 스냅샷 존재 → Optional.of(response)")
    void getMonthlyKeywordAnalysis_정상조회() {
        // given
        Document snapshot = createSnapshotWithKeywordSummary();
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        // when
        Optional<KeywordAnalysisResponse> result =
                service.getMonthlyKeywordAnalysis(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTopKeywords()).hasSize(1);
    }

    @Test
    @DisplayName("월간 스냅샷 없음 → Optional.empty()")
    void getMonthlyKeywordAnalysis_스냅샷없음() {
        // given
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(null);

        // when
        Optional<KeywordAnalysisResponse> result =
                service.getMonthlyKeywordAnalysis(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isEmpty();
    }
}
