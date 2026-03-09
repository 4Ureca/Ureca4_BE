package com.uplus.crm.domain.elasticsearch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.repository.ConsultElasticRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

/**
 * ConsultSearchService 단위 테스트.
 *
 * <p>핵심 검증 대상</p>
 * <ol>
 *   <li>원문(rawText) + IAM(iamIssue) 교차 AND 검색 — ES 쿼리 구조 및 필드 포함 여부</li>
 *   <li>rawTextJson → 평문 변환 ({@code extractPlainTextFromJson})</li>
 *   <li>토큰 분리 및 쿼리 타입 분기 (단일 vs 복수)</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class ConsultSearchServiceTest {

    @Mock
    ConsultElasticRepository consultElasticRepository;

    @Mock
    ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    ConsultSearchService service;

    // ─────────────────────────────────────────────────────────────────────
    // 원문 추출 — extractPlainTextFromJson
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractPlainTextFromJson — rawTextJson 평문 추출")
    class ExtractPlainTextTest {

        @Test
        @DisplayName("대화형 JSON: 발화자별 'text' 값을 모두 추출하여 평문 반환")
        void 대화형JSON_발화텍스트_추출() {
            String rawTextJson = """
                {
                  "turns": [
                    { "speaker": "agent",    "text": "안녕하세요 유플러스입니다" },
                    { "speaker": "customer", "text": "해지하고 싶어요" },
                    { "speaker": "agent",    "text": "아이폰으로 기기변경 안내드리겠습니다" }
                  ]
                }
                """;

            String result = service.extractPlainTextFromJson(rawTextJson);

            assertThat(result)
                    .as("원문에서 고객 발화 '해지하고 싶어요' 포함")
                    .contains("해지하고 싶어요");
            assertThat(result)
                    .as("상담사 발화 '아이폰으로 기기변경 안내드리겠습니다' 포함")
                    .contains("아이폰으로 기기변경 안내드리겠습니다");
            assertThat(result)
                    .as("인사말 포함")
                    .contains("안녕하세요 유플러스입니다");
        }

        @Test
        @DisplayName("중첩 JSON 구조에서도 재귀적으로 문자열 추출")
        void 중첩JSON_재귀추출() {
            String rawTextJson = """
                {
                  "session": {
                    "id": "S001",
                    "dialog": [
                      { "utterance": { "text": "갤럭시 해지 문의입니다" } },
                      { "utterance": { "text": "번호이동 원합니다" } }
                    ]
                  }
                }
                """;

            String result = service.extractPlainTextFromJson(rawTextJson);

            assertThat(result).contains("갤럭시 해지 문의입니다");
            assertThat(result).contains("번호이동 원합니다");
        }

        @Test
        @DisplayName("null 입력 → 빈 문자열 반환")
        void null입력_빈문자열() {
            assertThat(service.extractPlainTextFromJson(null)).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 입력 → 빈 문자열 반환")
        void 빈문자열_빈문자열() {
            assertThat(service.extractPlainTextFromJson("")).isEmpty();
        }

        @Test
        @DisplayName("유효하지 않은 JSON → 원본 그대로 반환 (예외 없음)")
        void 잘못된JSON_원본반환() {
            String invalid = "이건 JSON이 아닙니다";
            assertThat(service.extractPlainTextFromJson(invalid)).isEqualTo(invalid);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 핵심: 원문 단어 + IAM 단어 교차 AND 검색
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchConsultIdsByKeyword — 원문·IAM 교차 AND 검색")
    class CrossFieldSearchTest {

        /**
         * 시나리오
         *  - 검색어: "해지 아이폰"
         *  - "해지"  : 원문(rawText)에서 추출한 단어
         *  - "아이폰": IAM(iamIssue)에서 추출한 단어
         *  - 두 단어가 모두 포함된 ConsultDoc(consultId=1L) → 반환 기대
         */
        @Test
        @DisplayName("[메인] 원문단어(해지) + IAM단어(아이폰) → consultId=1 반환")
        void 원문단어_IAM단어_AND검색_consultId반환() {
            // given — 원문·IAM 모두 갖춘 상담 문서
            ConsultDoc doc = ConsultDoc.builder()
                    .id("es-doc-1")
                    .consultId(1L)
                    .rawText("고객이 서비스 해지하고 싶다고 함")    // 원문
                    .iamIssue("아이폰16 기기변경 및 선택약정 안내") // IAM
                    .build();

            // mock 생성을 given() 외부에서 먼저 수행
            SearchHits<ConsultDoc> hits = buildMockHits(doc);
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            // when — "해지"(원문) + "아이폰"(IAM) 복합 키워드
            List<Long> result = service.searchConsultIdsByKeyword("해지 아이폰");

            // then
            assertThat(result)
                    .as("원문 단어 '해지'와 IAM 단어 '아이폰'을 모두 포함한 문서의 consultId 반환")
                    .containsExactly(1L);
        }

        @Test
        @DisplayName("[쿼리 구조] 복합 키워드 → bool.must 2개 (AND 조건)")
        void 복합키워드_must2개_AND구조_확인() {
            // given
            SearchHits<ConsultDoc> hits = buildMockHits();
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            service.searchConsultIdsByKeyword("해지 아이폰");

            // then — ES 쿼리 캡처 및 must 절 개수 검증
            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            Query esQuery = captor.getValue().getQuery();

            assertThat(esQuery.isBool())
                    .as("최상위 쿼리는 bool")
                    .isTrue();
            assertThat(esQuery.bool().must())
                    .as("'해지'와 '아이폰' 각 토큰이 별개의 must 절 → AND 조건")
                    .hasSize(2);
            assertThat(esQuery.bool().should())
                    .as("복수 토큰 쿼리에서는 should 절 없음")
                    .isEmpty();
        }

        @Test
        @DisplayName("[rawText 필드] 각 must 절이 multiMatch이고 rawText·iamIssue 필드 포함")
        void 각_must절_multiMatch이며_rawText와_iamIssue_필드포함() {
            // given
            SearchHits<ConsultDoc> hits = buildMockHits();
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            service.searchConsultIdsByKeyword("해지 아이폰");

            // then
            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            List<Query> mustClauses = captor.getValue().getQuery().bool().must();

            mustClauses.forEach(must -> {
                assertThat(must.isMultiMatch())
                        .as("must 절은 multiMatch 쿼리")
                        .isTrue();
                assertThat(must.multiMatch().fields())
                        .as("rawText 필드 포함 → 원문(consultation_raw_texts) 검색 보장")
                        .anyMatch(f -> f.startsWith("rawText"));
                assertThat(must.multiMatch().fields())
                        .as("iamIssue 필드 포함 → IAM 검색 보장")
                        .anyMatch(f -> f.startsWith("iamIssue"));
            });
        }

        @Test
        @DisplayName("[중복 제거] 같은 consultId 여러 히트 → 하나만 반환")
        void 동일consultId_중복히트_하나만반환() {
            // given — consultId=5 가 두 개의 SearchHit으로 반환
            ConsultDoc doc1 = ConsultDoc.builder().id("hit-1").consultId(5L).build();
            ConsultDoc doc2 = ConsultDoc.builder().id("hit-2").consultId(5L).build();

            SearchHits<ConsultDoc> hits = buildMockHits(doc1, doc2);
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            // when
            List<Long> result = service.searchConsultIdsByKeyword("해지 아이폰");

            // then
            assertThat(result)
                    .as("동일 consultId는 distinct → 하나만 반환")
                    .containsExactly(5L);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 단일 토큰 검색 구조
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchConsultIdsByKeyword — 단일 토큰 쿼리 구조")
    class SingleTokenSearchTest {

        @Test
        @DisplayName("단일 키워드 → bool.should 3개 (BestFields + Phrase + PhrasePrefix)")
        void 단일키워드_should3개_구조() {
            // given
            ConsultDoc doc = ConsultDoc.builder().id("d1").consultId(10L).build();
            SearchHits<ConsultDoc> hits = buildMockHits(doc);
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            List<Long> result = service.searchConsultIdsByKeyword("아이폰");

            // then
            assertThat(result).containsExactly(10L);

            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            Query esQuery = captor.getValue().getQuery();

            assertThat(esQuery.isBool()).isTrue();
            assertThat(esQuery.bool().must())
                    .as("단일 토큰: must 절 없음")
                    .isEmpty();
            assertThat(esQuery.bool().should())
                    .as("단일 토큰: BestFields + Phrase + PhrasePrefix = 3개 should 절")
                    .hasSize(3);
        }

        @Test
        @DisplayName("단일 토큰 BestFields 쿼리에도 rawText 필드 포함")
        void 단일토큰_BestFields_rawText_포함() {
            // given
            SearchHits<ConsultDoc> hits = buildMockHits();
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            service.searchConsultIdsByKeyword("해지");

            // then
            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            Query bestFieldsShould = captor.getValue().getQuery().bool().should().get(0);

            assertThat(bestFieldsShould.isMultiMatch()).isTrue();
            assertThat(bestFieldsShould.multiMatch().fields())
                    .as("단일 토큰 BestFields에도 rawText 포함")
                    .anyMatch(f -> f.startsWith("rawText"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 빈 입력 처리
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchConsultIdsByKeyword — 빈 입력 처리")
    class EmptyKeywordTest {

        @Test
        @DisplayName("null 키워드 → 빈 목록 반환, ES 미호출")
        void null키워드_빈목록_ES미호출() {
            List<Long> result = service.searchConsultIdsByKeyword(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(elasticsearchOperations);
        }

        @Test
        @DisplayName("공백 키워드 → 빈 목록 반환, ES 미호출")
        void 공백키워드_빈목록_ES미호출() {
            assertThat(service.searchConsultIdsByKeyword("   ")).isEmpty();
            verifyNoInteractions(elasticsearchOperations);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 구체적 단어 쌍: 원문(갑질) + IAM(반복민원)
    //
    // 실제 C005 상담 케이스 기반
    //  - 원문(rawText): 고객이 실제 통화 중 발화한 내용에서 "갑질" 추출
    //  - IAM(iamIssue): 상담사가 작성한 "폭언 욕설 고객 응대 - 반복민원 고질민원"에서 "반복민원" 추출
    // ─────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("구체적 단어 쌍 — 원문(갑질) + IAM(반복민원)")
    class SpecificWordPairTest {

        // ── C005 케이스 실제 데이터 ────────────────────────────────────────

        /** 원문에서 선택한 단어 (consultation_raw_texts.raw_text_json 발화 내용) */
        private static final String 원문_단어 = "갑질";

        /** IAM에서 선택한 단어 (consultation_results.iam_issue 내용) */
        private static final String IAM_단어 = "반복민원";

        /**
         * C005 상담 원문 (consultation_raw_texts.raw_text_json 예시).
         * 실제 고객 발화에 "갑질" 포함.
         */
        private static final String C005_RAW_TEXT_JSON = """
                {
                  "sessionId": "S-C005",
                  "turns": [
                    { "seq": 1, "speaker": "agent",    "text": "안녕하세요 LG 유플러스 고객센터입니다." },
                    { "seq": 2, "speaker": "customer", "text": "야 이거 왜 이래요! 갑질 좀 그만해요!" },
                    { "seq": 3, "speaker": "agent",    "text": "죄송합니다 고객님, 최선을 다해 도와드리겠습니다." },
                    { "seq": 4, "speaker": "customer", "text": "또 이런 거야? 나 소비자원에 신고할 거야!" },
                    { "seq": 5, "speaker": "agent",    "text": "고객님 진정하시고 말씀해 주시면 해결해 드리겠습니다." }
                  ]
                }
                """;

        /**
         * C005 IAM 상담 결과서 (consultation_results.iam_issue 내용).
         * "반복민원" 포함.
         */
        private static final String C005_IAM_ISSUE = "폭언 욕설 고객 응대 - 반복민원 고질민원 이력 있음";

        /** C005 consultId */
        private static final Long C005_CONSULT_ID = 5L;

        // ── Step 1: 원문 추출 검증 ─────────────────────────────────────────

        @Test
        @DisplayName("Step 1 [원문 추출] rawTextJson에서 '갑질' 단어 추출 성공")
        void 원문_rawTextJson에서_갑질_추출() {
            // when
            String plainText = service.extractPlainTextFromJson(C005_RAW_TEXT_JSON);

            // then — 고객 발화 "갑질 좀 그만해요!"에서 "갑질" 추출됨
            assertThat(plainText)
                    .as("원문(rawText)에 고객 발화 '갑질'이 포함되어야 함")
                    .contains(원문_단어);

            // 원문 전체 내용 검증 (5개 발화 모두 포함)
            assertThat(plainText).contains("안녕하세요 LG 유플러스 고객센터입니다.");
            assertThat(plainText).contains("소비자원에 신고할 거야!");
        }

        // ── Step 2: IAM 필드 확인 ──────────────────────────────────────────

        @Test
        @DisplayName("Step 2 [IAM 확인] iamIssue에 '반복민원' 포함 여부")
        void IAM_iamIssue에_반복민원_포함() {
            assertThat(C005_IAM_ISSUE)
                    .as("IAM(iamIssue)에 '반복민원' 포함")
                    .contains(IAM_단어);
        }

        // ── Step 3: 핵심 교차 검색 테스트 ─────────────────────────────────

        @Test
        @DisplayName("Step 3 [메인] '갑질 반복민원' 키워드 → consultId=5 반환")
        void 갑질_반복민원_키워드검색_consultId5반환() {
            // given — Step 1에서 추출한 rawText + C005 iamIssue로 ConsultDoc 구성
            String rawText = service.extractPlainTextFromJson(C005_RAW_TEXT_JSON);

            ConsultDoc c005 = ConsultDoc.builder()
                    .id("c005-es")
                    .consultId(C005_CONSULT_ID)
                    .rawText(rawText)           // 원문: "갑질" 포함
                    .iamIssue(C005_IAM_ISSUE)  // IAM: "반복민원" 포함
                    .build();

            SearchHits<ConsultDoc> hits = buildMockHits(c005);
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            // when — 원문 단어("갑질") + IAM 단어("반복민원") 동시 입력
            String keyword = 원문_단어 + " " + IAM_단어; // "갑질 반복민원"
            List<Long> result = service.searchConsultIdsByKeyword(keyword);

            // then
            assertThat(result)
                    .as("원문(갑질)과 IAM(반복민원)이 모두 일치하는 C005 consultId 반환")
                    .containsExactly(C005_CONSULT_ID);
        }

        @Test
        @DisplayName("Step 4 [쿼리 구조] '갑질 반복민원' → bool.must 2개 AND 절")
        void 갑질_반복민원_must2개_AND쿼리_구조검증() {
            // given
            SearchHits<ConsultDoc> hits = buildMockHits();
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            service.searchConsultIdsByKeyword(원문_단어 + " " + IAM_단어);

            // then — ES 쿼리 구조 검증
            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            var boolQuery = captor.getValue().getQuery().bool();

            // AND 조건: "갑질"과 "반복민원" 각각 must 절
            assertThat(boolQuery.must())
                    .as("'갑질'(원문)과 '반복민원'(IAM) 각각 must 절 → 2개")
                    .hasSize(2);

            // 각 must 절의 query 값이 정확히 선택한 단어인지 확인
            List<String> queryValues = boolQuery.must().stream()
                    .map(q -> q.multiMatch().query())
                    .toList();
            assertThat(queryValues)
                    .as("must 절의 query 값이 '갑질'과 '반복민원'")
                    .containsExactlyInAnyOrder(원문_단어, IAM_단어);
        }

        @Test
        @DisplayName("Step 5 [필드 검증] 각 must 절에 rawText(원문)와 iamIssue(IAM) 모두 포함")
        void 갑질_반복민원_must절에_rawText_iamIssue_포함() {
            // given
            SearchHits<ConsultDoc> hits = buildMockHits();
            given(elasticsearchOperations.search(any(NativeQuery.class), eq(ConsultDoc.class)))
                    .willReturn(hits);

            ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);

            // when
            service.searchConsultIdsByKeyword(원문_단어 + " " + IAM_단어);

            // then
            verify(elasticsearchOperations).search(captor.capture(), eq(ConsultDoc.class));
            List<co.elastic.clients.elasticsearch._types.query_dsl.Query> mustClauses =
                    captor.getValue().getQuery().bool().must();

            mustClauses.forEach(must -> {
                List<String> fields = must.multiMatch().fields();

                assertThat(fields)
                        .as("원문 검색 필드(rawText) 포함 — consultation_raw_texts 원문 검색 보장")
                        .anyMatch(f -> f.startsWith("rawText"));

                assertThat(fields)
                        .as("IAM 검색 필드(iamIssue) 포함 — 상담 결과서 검색 보장")
                        .anyMatch(f -> f.startsWith("iamIssue"));
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // 헬퍼 — mock 생성과 given() 설정을 분리하여 UnfinishedStubbingException 방지
    // ─────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private SearchHits<ConsultDoc> buildMockHits(ConsultDoc... docs) {
        // 1. SearchHit mock 먼저 생성
        List<SearchHit<ConsultDoc>> hitList = Arrays.stream(docs)
                .map(doc -> {
                    SearchHit<ConsultDoc> hit = (SearchHit<ConsultDoc>) mock(SearchHit.class);
                    // 이 stub은 given()의 평가 범위 밖에서 설정됨
                    given(hit.getContent()).willReturn(doc);
                    return hit;
                })
                .toList();

        // 2. SearchHits mock 생성 후 stream() 설정
        SearchHits<ConsultDoc> hits = (SearchHits<ConsultDoc>) mock(SearchHits.class);
        // willAnswer: 호출마다 새 Stream 생성 (Stream은 1회 소비 가능)
        given(hits.stream()).willAnswer(inv -> hitList.stream());
        return hits;
    }
}
