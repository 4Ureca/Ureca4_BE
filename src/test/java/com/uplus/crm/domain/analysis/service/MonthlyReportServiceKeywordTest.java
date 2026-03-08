package com.uplus.crm.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
@DisplayName("MonthlyReportService 키워드 분석 테스트")
class MonthlyReportServiceKeywordTest {

    @Mock MongoTemplate mongoTemplate;
    @InjectMocks MonthlyReportService service;

    // ==================== 헬퍼 ====================

    private Document createSnapshotWithKeywordSummary() {
        Document topKw = new Document("keyword", "해지")
                .append("count", 150)
                .append("rank", 1)
                .append("changeRate", 12.3);

        Document longTerm = new Document("keyword", "요금제")
                .append("count", 500)
                .append("rank", 1)
                .append("appearanceDays", 25)
                .append("totalDays", 28);

        Document ct = new Document("customerType", "VIP")
                .append("keywords", List.of("해지", "요금제", "번호이동"));

        Document keywordSummary = new Document()
                .append("topKeywords", List.of(topKw))
                .append("longTermTopKeywords", List.of(longTerm))
                .append("byCustomerType", List.of(ct));

        return new Document()
                .append("startAt", LocalDateTime.of(2025, 1, 1, 0, 0, 0))
                .append("endAt", LocalDateTime.of(2025, 1, 31, 23, 59, 59))
                .append("keywordSummary", keywordSummary);
    }

    // ==================== 테스트 ====================

    @Test
    @DisplayName("월별 스냅샷 존재 → KeywordAnalysisResponse 반환")
    void getMonthlyKeywordAnalysis_정상조회() {
        // given
        Document snapshot = createSnapshotWithKeywordSummary();
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        // when
        KeywordAnalysisResponse result =
                service.getMonthlyKeywordAnalysis(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo("2025-01-01");
        assertThat(result.getEndDate()).isEqualTo("2025-01-31");
        assertThat(result.getTopKeywords()).hasSize(1);
        assertThat(result.getTopKeywords().get(0).getKeyword()).isEqualTo("해지");
        assertThat(result.getTopKeywords().get(0).getCount()).isEqualTo(150);
        assertThat(result.getTopKeywords().get(0).getChangeRate()).isEqualTo(12.3);
        assertThat(result.getLongTermTopKeywords()).hasSize(1);
        assertThat(result.getLongTermTopKeywords().get(0).getAppearanceDays()).isEqualTo(25);
        assertThat(result.getByCustomerType()).hasSize(1);
        assertThat(result.getByCustomerType().get(0).getCustomerType()).isEqualTo("VIP");
    }

    @Test
    @DisplayName("월별 스냅샷 없음 → null 반환")
    void getMonthlyKeywordAnalysis_스냅샷없음() {
        // given
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(null);

        // when
        KeywordAnalysisResponse result =
                service.getMonthlyKeywordAnalysis(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("스냅샷 존재하나 keywordSummary 필드 없음 → null 반환")
    void getMonthlyKeywordAnalysis_keywordSummary없음() {
        // given
        Document snapshot = new Document("someField", "value");
        given(mongoTemplate.findOne(any(Query.class), eq(Document.class), eq("monthly_report_snapshot")))
                .willReturn(snapshot);

        // when
        KeywordAnalysisResponse result =
                service.getMonthlyKeywordAnalysis(LocalDate.of(2025, 1, 15));

        // then
        assertThat(result).isNull();
    }
}
