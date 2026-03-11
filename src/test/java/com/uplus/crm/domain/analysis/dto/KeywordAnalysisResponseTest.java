package com.uplus.crm.domain.analysis.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("KeywordAnalysisResponse DTO 매핑 테스트")
class KeywordAnalysisResponseTest {

    // ==================== 헬퍼 메서드 ====================

    private Document createFullSnapshot() {
        Document topKw1 = new Document("keyword", "해지")
                .append("count", 50)
                .append("rank", 1)
                .append("changeRate", 25.5);
        Document topKw2 = new Document("keyword", "요금제")
                .append("count", 30)
                .append("rank", 2)
                .append("changeRate", -10.0);

        Document longTerm1 = new Document("keyword", "해지")
                .append("count", 320)
                .append("rank", 1)
                .append("appearanceDays", 20)
                .append("totalDays", 28);

        Document ct1 = new Document("customerType", "VIP")
                .append("keywords", List.of("해지", "요금제", "번호이동"));
        Document ct2 = new Document("customerType", "DIAMOND")
                .append("keywords", List.of("기기변경", "위약금"));

        Document keywordSummary = new Document()
                .append("topKeywords", List.of(topKw1, topKw2))
                .append("longTermTopKeywords", List.of(longTerm1))
                .append("byCustomerType", List.of(ct1, ct2));

        return new Document()
                .append("startAt", LocalDateTime.of(2025, 1, 8, 0, 0, 0))
                .append("endAt", LocalDateTime.of(2025, 1, 15, 23, 59, 59))
                .append("keywordSummary", keywordSummary);
    }

    // ==================== 테스트 ====================

    @Nested
    @DisplayName("정상 매핑")
    class NormalMapping {

        @Test
        @DisplayName("keywordSummary 전체 필드 정상 매핑")
        void from_전체필드_정상매핑() {
            // given
            Document snapshot = createFullSnapshot();

            // when
            KeywordAnalysisResponse result = KeywordAnalysisResponse.from(snapshot);

            // then
            assertThat(result).isNotNull();

            // topKeywords
            assertThat(result.getTopKeywords()).hasSize(2);
            KeywordAnalysisResponse.TopKeyword top1 = result.getTopKeywords().get(0);
            assertThat(top1.getKeyword()).isEqualTo("해지");
            assertThat(top1.getCount()).isEqualTo(50);
            assertThat(top1.getRank()).isEqualTo(1);
            assertThat(top1.getChangeRate()).isEqualTo(25.5);

            KeywordAnalysisResponse.TopKeyword top2 = result.getTopKeywords().get(1);
            assertThat(top2.getKeyword()).isEqualTo("요금제");
            assertThat(top2.getCount()).isEqualTo(30);
            assertThat(top2.getChangeRate()).isEqualTo(-10.0);

            // longTermTopKeywords
            assertThat(result.getLongTermTopKeywords()).hasSize(1);
            KeywordAnalysisResponse.LongTermKeyword lt1 = result.getLongTermTopKeywords().get(0);
            assertThat(lt1.getKeyword()).isEqualTo("해지");
            assertThat(lt1.getCount()).isEqualTo(320);
            assertThat(lt1.getRank()).isEqualTo(1);
            assertThat(lt1.getAppearanceDays()).isEqualTo(20);
            assertThat(lt1.getTotalDays()).isEqualTo(28);

            // byCustomerType (레거시 List<String> 포맷 → CustomerKeywordCount(count=0) 변환 검증)
            assertThat(result.getByCustomerType()).hasSize(2);
            assertThat(result.getByCustomerType().get(0).getCustomerType()).isEqualTo("VIP");
            List<KeywordAnalysisResponse.CustomerKeywordCount> vipKws =
                    result.getByCustomerType().get(0).getKeywords();
            assertThat(vipKws).hasSize(3);
            assertThat(vipKws.get(0).getKeyword()).isEqualTo("해지");
            assertThat(vipKws.get(0).getCount()).isEqualTo(0); // 레거시 포맷은 count 없음
            assertThat(vipKws.get(1).getKeyword()).isEqualTo("요금제");
            assertThat(vipKws.get(2).getKeyword()).isEqualTo("번호이동");
            assertThat(result.getByCustomerType().get(1).getCustomerType()).isEqualTo("DIAMOND");
        }

        @Test
        @DisplayName("LocalDateTime → String 날짜 변환")
        void from_startDate_endDate_변환() {
            // given
            Document snapshot = createFullSnapshot();

            // when
            KeywordAnalysisResponse result = KeywordAnalysisResponse.from(snapshot);

            // then
            assertThat(result.getStartDate()).isEqualTo("2025-01-08");
            assertThat(result.getEndDate()).isEqualTo("2025-01-15");
        }

        @Test
        @DisplayName("count가 Integer/Long 타입일 때 long으로 정상 변환")
        void from_count_Number타입_처리() {
            // given — Integer와 Long 혼합
            Document topKwInt = new Document("keyword", "해지")
                    .append("count", 5)           // Integer
                    .append("rank", 1)
                    .append("changeRate", 0.0);
            Document topKwLong = new Document("keyword", "요금제")
                    .append("count", 100L)         // Long
                    .append("rank", 2)
                    .append("changeRate", 0.0);

            Document keywordSummary = new Document("topKeywords", List.of(topKwInt, topKwLong));
            Document snapshot = new Document("keywordSummary", keywordSummary)
                    .append("startAt", LocalDateTime.of(2025, 1, 8, 0, 0));

            // when
            KeywordAnalysisResponse result = KeywordAnalysisResponse.from(snapshot);

            // then
            assertThat(result.getTopKeywords().get(0).getCount()).isEqualTo(5L);
            assertThat(result.getTopKeywords().get(1).getCount()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("null/빈 데이터 처리")
    class NullHandling {

        @Test
        @DisplayName("snapshot이 null이면 null 반환")
        void from_snapshot_null_반환null() {
            assertThat(KeywordAnalysisResponse.from(null)).isNull();
        }

        @Test
        @DisplayName("keywordSummary 필드 없으면 null 반환")
        void from_keywordSummary_없으면_null() {
            // given
            Document snapshot = new Document("someOtherField", "value");

            // when & then
            assertThat(KeywordAnalysisResponse.from(snapshot)).isNull();
        }

        @Test
        @DisplayName("topKeywords null이면 빈 리스트")
        void from_topKeywords_null_빈리스트() {
            // given — keywordSummary는 존재하지만 topKeywords 없음
            Document keywordSummary = new Document("someField", "value");
            Document snapshot = new Document("keywordSummary", keywordSummary);

            // when
            KeywordAnalysisResponse result = KeywordAnalysisResponse.from(snapshot);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTopKeywords()).isEmpty();
            assertThat(result.getLongTermTopKeywords()).isEmpty();
            assertThat(result.getByCustomerType()).isEmpty();
        }

        @Test
        @DisplayName("byCustomerType의 keywords가 null이면 빈 리스트")
        void from_keywords_null_빈리스트() {
            // given — keywords 필드가 null인 customerType
            Document ct = new Document("customerType", "VIP");
            // keywords 필드를 넣지 않음 → null

            Document keywordSummary = new Document("byCustomerType", List.of(ct));
            Document snapshot = new Document("keywordSummary", keywordSummary);

            // when
            KeywordAnalysisResponse result = KeywordAnalysisResponse.from(snapshot);

            // then
            assertThat(result.getByCustomerType()).hasSize(1);
            assertThat(result.getByCustomerType().get(0).getKeywords()).isEmpty();
        }
    }
}
