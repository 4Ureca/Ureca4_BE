package com.uplus.crm.domain.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.repository.ConsultElasticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultSearchService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ── 인삿말 감지 패턴 (응대품질 분석용) ─────────────────────────────────────
    private static final List<String> GREETING_PATTERNS = List.of(
            "안녕하세요", "안녕하십니까", "반갑습니다", "좋은 아침");

    private static final List<String> FAREWELL_PATTERNS = List.of(
            "감사합니다", "감사드립니다", "수고하세요", "수고하셨습니다",
            "고맙습니다", "즐거운 하루", "좋은 하루", "안녕히 계세요");

    /**
     * 키워드 검색 대상 필드 및 가중치.
     *
     * <ul>
     *   <li>iamIssue^3.0  — AI 추출 상담 이슈 (가장 핵심)</li>
     *   <li>iamAction^2.0 — AI 추출 조치사항</li>
     *   <li>content^1.5   — 상담 요약 내용</li>
     *   <li>iamMemo^1.0   — 상담 특이사항</li>
     *   <li>allText^1.0   — 통합 텍스트</li>
     *   <li>rawText^1.0   — 상담 원문 (consultation_raw_texts.raw_text_json 평문 변환)</li>
     *   <li>customerName  — 고객명</li>
     * </ul>
     */
    private static final List<String> FULL_FIELDS = List.of(
            "iamIssue^3.0",
            "iamAction^2.0",
            "content^1.5",
            "iamMemo",
            "allText",
            "rawText",
            "customerName"
    );

    private final ConsultElasticRepository consultElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 상담 데이터 저장 (Indexing).
     *
     * <p>저장 전 자동 처리:</p>
     * <ul>
     *   <li>인삿말·마무리 인사 포함 여부 감지 → {@code hasGreeting}, {@code hasFarewell}</li>
     *   <li>{@code rawText} 가 설정되지 않았으면 {@code content + allText} 로 fallback</li>
     * </ul>
     *
     * <p><b>rawText 설정 방법 (인덱싱 호출 측)</b></p>
     * <pre>
     *   String plain = consultSearchService.extractPlainTextFromJson(rawTextJson);
     *   consultDoc.setRawText(plain);
     *   consultSearchService.saveConsultation(consultDoc);
     * </pre>
     */
    public void saveConsultation(ConsultDoc consultDoc) {
        String greetingTarget = buildGreetingTarget(consultDoc);
        consultDoc.setHasGreeting(containsAny(greetingTarget, GREETING_PATTERNS));
        consultDoc.setHasFarewell(containsAny(greetingTarget, FAREWELL_PATTERNS));
        consultElasticRepository.save(consultDoc);
    }

    /**
     * 상담사 발화 텍스트를 기준으로 인삿말·마무리 인사를 감지하여 저장.
     *
     * <p>실제 대화원문(consultation_raw_texts)에서 상담사 발화만 추출한 텍스트를 전달하면
     * 고객 발화의 "감사합니다" 등이 오탐되지 않는다.</p>
     *
     * @param consultDoc  저장할 ES 문서 (rawText는 전체 대화 평문으로 미리 세팅)
     * @param agentText   품질 감지 전용 상담사 발화 텍스트
     */
    public void saveConsultation(ConsultDoc consultDoc, String agentText) {
        String greetingSource = (agentText != null && !agentText.isBlank())
                ? agentText
                : buildGreetingTarget(consultDoc);
        consultDoc.setHasGreeting(containsAny(greetingSource, GREETING_PATTERNS));
        consultDoc.setHasFarewell(containsAny(greetingSource, FAREWELL_PATTERNS));
        consultElasticRepository.save(consultDoc);
    }

    /**
     * {@code consultation_raw_texts.raw_text_json} 을 ES 검색용 평문으로 변환.
     *
     * <p>JSON 구조가 어떻게 되어 있든 모든 문자열 값(value)을 재귀적으로 추출하여
     * 공백으로 연결한다. 파싱 실패 시 원본 문자열을 그대로 반환.</p>
     *
     * <pre>
     * 예시 JSON:
     * {"turns": [{"speaker":"agent","text":"안녕하세요"}, {"speaker":"customer","text":"해지하고 싶어요"}]}
     * → "안녕하세요 해지하고 싶어요"
     * </pre>
     *
     * @param rawTextJson consultation_raw_texts.raw_text_json
     * @return 검색 가능한 평문 (null/빈값이면 빈 문자열)
     */
    /**
     * 대화원문 JSON에서 상담사 발화 텍스트만 추출 (응대품질 분석 전용).
     *
     * <p>지원하는 JSON 구조:</p>
     * <pre>
     * {"turns": [{"speaker":"agent","text":"안녕하세요"}, {"speaker":"customer","text":"해지하고 싶어요"}]}
     * [{"speaker":"상담사","text":"..."}, {"speaker":"고객","text":"..."}]
     * </pre>
     *
     * <p>speaker 필드에 "agent", "상담사", "advisor", "consultant" 중 하나가 포함된 턴만 추출한다.
     * 구조를 인식할 수 없는 경우 빈 문자열 반환 → 호출 측이 fallback 처리.</p>
     *
     * @param rawTextJson consultation_raw_texts.raw_text_json
     * @return 상담사 발화만 이어 붙인 평문 (인식 실패 시 빈 문자열)
     */
    public String extractAgentTextFromJson(String rawTextJson) {
        if (rawTextJson == null || rawTextJson.isBlank()) return "";
        try {
            JsonNode root = OBJECT_MAPPER.readTree(rawTextJson);
            JsonNode turns = root.isArray() ? root : root.path("turns");
            if (!turns.isMissingNode() && turns.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode turn : turns) {
                    String speaker = turn.path("speaker").asText(
                            turn.path("role").asText(""));
                    if (isAgentSpeaker(speaker)) {
                        String text = turn.path("text").asText(
                                turn.path("content").asText(""));
                        if (!text.isBlank()) sb.append(text).append(' ');
                    }
                }
                return sb.toString().trim();
            }
            return "";
        } catch (Exception e) {
            log.warn("[ConsultSearch] agentText 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    private boolean isAgentSpeaker(String speaker) {
        if (speaker == null) return false;
        String s = speaker.toLowerCase();
        return s.contains("agent") || s.contains("상담사")
                || s.contains("advisor") || s.contains("consultant");
    }

    public String extractPlainTextFromJson(String rawTextJson) {
        if (rawTextJson == null || rawTextJson.isBlank()) return "";
        try {
            JsonNode root = OBJECT_MAPPER.readTree(rawTextJson);
            StringBuilder sb = new StringBuilder();
            collectStrings(root, sb);
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("[ConsultSearch] rawTextJson 파싱 실패 — 원본 반환: {}", e.getMessage());
            return rawTextJson;
        }
    }

    /** JSON 노드를 재귀 탐색하여 모든 문자열 값을 추출 */
    private void collectStrings(JsonNode node, StringBuilder sb) {
        if (node.isTextual()) {
            sb.append(node.asText()).append(' ');
        } else if (node.isArray()) {
            node.forEach(child -> collectStrings(child, sb));
        } else if (node.isObject()) {
            node.fields().forEachRemaining(entry -> collectStrings(entry.getValue(), sb));
        }
    }

    private String buildGreetingTarget(ConsultDoc doc) {
        StringBuilder sb = new StringBuilder();
        if (doc.getContent() != null)  sb.append(doc.getContent()).append(' ');
        if (doc.getAllText() != null)   sb.append(doc.getAllText()).append(' ');
        if (doc.getRawText() != null)   sb.append(doc.getRawText());
        return sb.toString();
    }

    private boolean containsAny(String text, List<String> patterns) {
        if (text == null || text.isBlank()) return false;
        return patterns.stream().anyMatch(text::contains);
    }

    /**
     * 복합 키워드 교차 검색 후 consultId 목록 반환 (MongoDB Hybrid 조인용).
     *
     * <p><b>토큰화 전략</b></p>
     * <ul>
     *   <li>공백 분리 → 각 토큰을 별개의 {@code must} 절로 처리 (AND 조건)</li>
     *   <li>단일 토큰: BestFields + Phrase 조합 (기존 고정밀 방식)</li>
     *   <li>복수 토큰: 토큰별 must[multi_match(all_fields)] → 교집합 반환</li>
     * </ul>
     *
     * <p><b>예시</b></p>
     * <pre>
     *   "그만 아이폰"
     *   → must[ multi_match("그만",  [content, allText, iamIssue, ...]) ]
     *     must[ multi_match("아이폰", [content, allText, iamIssue, ...]) ]
     *   → 두 단어 모두 문서 내 어딘가에 존재하는 상담만 반환
     * </pre>
     *
     * @param keyword 검색어 (공백 구분 복합 키워드 가능)
     * @return 매칭된 consultId 목록 (최대 1000건)
     */
    public List<Long> searchConsultIdsByKeyword(String keyword) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return List.of();

        NativeQuery query = (tokens.size() == 1)
                ? buildSingleTokenQuery(tokens.get(0))
                : buildCrossTokenQuery(tokens);

        return extractConsultIds(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 단일 토큰: BestFields + Phrase 조합 쿼리.
     * 동의어 사전 + 오타 허용(fuzziness) + 구문 일치(Phrase) boost.
     */
    private NativeQuery buildSingleTokenQuery(String token) {
        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                // 주 검색: 전 필드 BestFields (오타 허용)
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields(FULL_FIELDS)
                                                .query(token)
                                                .type(TextQueryType.BestFields)
                                                .fuzziness("AUTO")
                                                .minimumShouldMatch("1")
                                        )
                                )
                                // 구문 검색: 정확한 순서 매칭 시 boost
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields("iamIssue^4.0", "iamAction^2.5", "content^2.0", "allText")
                                                .query(token)
                                                .type(TextQueryType.Phrase)
                                                .boost(2.0f)
                                        )
                                )
                                // 부분 일치: prefix 검색 (edge n-gram 미적용 환경 보완)
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields("iamIssue^2.0", "iamAction^1.5", "content^1.0", "allText")
                                                .query(token)
                                                .type(TextQueryType.PhrasePrefix)
                                        )
                                )
                                .minimumShouldMatch("1")
                        )
                )
                .withPageable(PageRequest.of(0, 1000))
                .build();
    }

    /**
     * 복수 토큰 교차 AND 쿼리.
     *
     * <p>각 토큰이 {@code must} 절로 변환되어 모든 토큰이 문서 내에 존재해야 함.
     * 각 {@code must} 내부는 전 필드 OR 검색(multi_match BestFields).</p>
     *
     * <pre>
     * bool: {
     *   must: [
     *     multi_match(token1, all_fields),  // 토큰1: 어느 필드든 존재해야 함
     *     multi_match(token2, all_fields),  // 토큰2: 어느 필드든 존재해야 함
     *   ]
     * }
     * </pre>
     */
    private NativeQuery buildCrossTokenQuery(List<String> tokens) {
        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            tokens.forEach(token ->
                                    b.must(m -> m
                                            .multiMatch(mm -> mm
                                                    .fields(FULL_FIELDS)
                                                    .query(token)
                                                    .type(TextQueryType.BestFields)
                                                    .fuzziness("AUTO")
                                                    .minimumShouldMatch("1")
                                            )
                                    )
                            );
                            return b;
                        })
                )
                .withPageable(PageRequest.of(0, 1000))
                .build();
    }

    /**
     * AND 우선 / OR 보완 검색 결과.
     *
     * @param andMatchIds  모든 토큰이 일치하는 문서의 consultId (우선 노출)
     * @param orOnlyMatchIds 일부 토큰만 일치하는 문서의 consultId (후순위 노출)
     */
    public record KeywordSearchResult(List<Long> andMatchIds, List<Long> orOnlyMatchIds) {
        public boolean isEmpty() {
            return andMatchIds.isEmpty() && orOnlyMatchIds.isEmpty();
        }
    }

    /**
     * AND 우선 + OR 보완 검색.
     *
     * <p>복수 토큰 입력 시 두 번의 ES 검색을 수행한다:</p>
     * <ol>
     *   <li>AND 검색 (모든 토큰 must) → {@code andMatchIds}</li>
     *   <li>OR 검색 (최소 1개 토큰 should) → AND에 없는 ID만 {@code orOnlyMatchIds}</li>
     * </ol>
     *
     * <p>단일 토큰은 OR와 AND 구분이 없으므로 {@code andMatchIds}에만 담아 반환.</p>
     *
     * @param keyword 공백 구분 복합 키워드
     */
    public KeywordSearchResult searchWithPriority(String keyword) {
        List<String> tokens = tokenize(keyword);
        if (tokens.isEmpty()) return new KeywordSearchResult(List.of(), List.of());

        // 단일 토큰: AND/OR 구분 없음
        if (tokens.size() == 1) {
            List<Long> ids = extractConsultIds(
                    elasticsearchOperations.search(buildSingleTokenQuery(tokens.get(0)), ConsultDoc.class));
            return new KeywordSearchResult(ids, List.of());
        }

        // AND IDs: 모든 토큰 포함
        List<Long> andIds = extractConsultIds(
                elasticsearchOperations.search(buildCrossTokenQuery(tokens), ConsultDoc.class));

        // OR IDs: 최소 1개 토큰 포함
        List<Long> orIds = extractConsultIds(
                elasticsearchOperations.search(buildOrTokenQuery(tokens), ConsultDoc.class));

        // OR-only: OR에 있지만 AND에 없는 것
        Set<Long> andSet = new HashSet<>(andIds);
        List<Long> orOnlyIds = orIds.stream()
                .filter(id -> !andSet.contains(id))
                .toList();

        return new KeywordSearchResult(andIds, orOnlyIds);
    }

    /**
     * 복수 토큰 OR 쿼리.
     * 각 토큰을 {@code should} 절로 처리 — 최소 1개 이상 일치해야 함.
     */
    private NativeQuery buildOrTokenQuery(List<String> tokens) {
        return NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            tokens.forEach(token ->
                                    b.should(s -> s
                                            .multiMatch(mm -> mm
                                                    .fields(FULL_FIELDS)
                                                    .query(token)
                                                    .type(TextQueryType.BestFields)
                                                    .fuzziness("AUTO")
                                                    .minimumShouldMatch("1")
                                            )
                                    )
                            );
                            b.minimumShouldMatch("1");
                            return b;
                        })
                )
                .withPageable(PageRequest.of(0, 1000))
                .build();
    }

    /**
     * 공백 기준 토큰화.
     * - 빈 문자열 제거
     * - 중복 제거
     * - 불용어 처리는 ES 분석기 위임 (stopwords.txt)
     */
    private List<String> tokenize(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return Arrays.stream(keyword.trim().split("[\\s　]+"))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();
    }

    /** SearchHits → consultId 목록 추출 */
    private List<Long> extractConsultIds(SearchHits<ConsultDoc> hits) {
        return hits.stream()
                .map(SearchHit::getContent)
                .map(ConsultDoc::getConsultId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 통합 키워드 검색 (페이징 기본값 적용)
     * - 동의어 사전 적용: 갤폰→갤럭시, 번이→번호이동, 넷플→넷플릭스 등
     * - 오타 허용 (fuzziness AUTO)
     * - iamIssue 가중치 3배, iamAction 2배, content 1.5배
     */
    public List<ConsultDoc> searchByKeyword(String keyword) {
        return searchByKeyword(keyword, 0, 20);
    }

    /**
     * 통합 키워드 검색 (페이징 지원)
     * BestFields + Phrase 쿼리 조합으로 정확도 향상
     */
    public List<ConsultDoc> searchByKeyword(String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                // 주 검색: 동의어 분석 + 오타 허용
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields(FULL_FIELDS)
                                                .query(keyword)
                                                .type(TextQueryType.BestFields)
                                                .fuzziness("AUTO")
                                                .minimumShouldMatch("1")
                                        )
                                )
                                // 구문 검색: 정확한 단어 순서 → 높은 점수
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields("iamIssue^4.0", "iamAction^2.5", "content^2.0", "allText")
                                                .query(keyword)
                                                .type(TextQueryType.Phrase)
                                                .boost(2.0f)
                                        )
                                )
                                .minimumShouldMatch("1")
                        )
                )
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 고객 ID로 상담 이력 조회 (최신순)
     */
    public List<ConsultDoc> findByCustomerId(String customerId) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t.field("customerId").value(customerId))
                )
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 고위험 상담 검색 (riskScore >= threshold OR 위험 키워드)
     * 해지위험, 폭언욕설, 반복민원 등 동의어 사전 적용
     */
    public List<ConsultDoc> searchHighRisk(String keyword, int riskThreshold) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .should(s -> s
                                        .multiMatch(m -> m
                                                .fields("iamIssue^3.0", "content^2.0", "allText")
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        )
                                )
                                .should(s -> s
                                        .range(r -> r
                                                .number(n -> n
                                                        .field("riskScore")
                                                        .gte((double) riskThreshold)
                                                )
                                        )
                                )
                                .minimumShouldMatch("1")
                        )
                )
                .withSort(Sort.by(Sort.Direction.DESC, "riskScore"))
                .withPageable(PageRequest.of(0, 50))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 고위험 상담 검색 (기본 임계값 70)
     */
    public List<ConsultDoc> searchHighRisk(String keyword) {
        return searchHighRisk(keyword, 70);
    }

    /**
     * 감정 분류 + 키워드 복합 검색
     * @param sentiment POSITIVE / NEGATIVE / NEUTRAL
     */
    public List<ConsultDoc> searchBySentimentAndKeyword(String sentiment, String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // 감정 필터 (must)
                            b.must(m -> m.term(t -> t.field("sentiment").value(sentiment)));
                            // 키워드 검색 (should)
                            if (keyword != null && !keyword.isBlank()) {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .fields(FULL_FIELDS)
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        )
                                );
                            }
                            return b;
                        })
                )
                .withSort(Sort.by(Sort.Direction.DESC, "riskScore"))
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 날짜 범위 + 키워드 복합 검색
     */
    public List<ConsultDoc> searchByDateRangeAndKeyword(
            String keyword, LocalDateTime from, LocalDateTime to, int page, int size) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            // 날짜 범위 필터
                            b.filter(f -> f
                                    .range(r -> r
                                            .date(d -> d
                                                    .field("createdAt")
                                                    .gte(from.format(DATE_FMT))
                                                    .lte(to.format(DATE_FMT))
                                                    .format("yyyy-MM-dd'T'HH:mm:ss")
                                            )
                                    )
                            );
                            // 키워드 검색
                            if (keyword != null && !keyword.isBlank()) {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .fields(FULL_FIELDS)
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        )
                                );
                            }
                            return b;
                        })
                )
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 고객명 검색 (부분 이름 검색 지원)
     * - 분석기 적용 match (오타 허용)
     * - 정확한 전체 이름 term 검색 (raw 필드)
     * - 부분 이름 wildcard 검색: 성을 제외한 이름만 입력해도 검색 가능
     */
    public List<ConsultDoc> searchByCustomerName(String customerName) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                // 분석기 적용 match (오타 허용)
                                .should(s -> s
                                        .match(m -> m
                                                .field("customerName")
                                                .query(customerName)
                                                .fuzziness("AUTO")
                                                .boost(1.5f)
                                        )
                                )
                                // 정확한 전체 이름 match (raw 필드)
                                .should(s -> s
                                        .term(t -> t
                                                .field("customerName.raw")
                                                .value(customerName)
                                                .boost(3.0f)
                                        )
                                )
                                // 부분 이름 wildcard 검색 (성 제외 이름만 입력 시에도 검색)
                                .should(s -> s
                                        .wildcard(w -> w
                                                .field("customerName.raw")
                                                .value("*" + customerName + "*")
                                                .boost(2.0f)
                                        )
                                )
                                .minimumShouldMatch("1")
                        )
                )
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 우선순위 필터 검색
     * @param priority URGENT / HIGH / NORMAL / LOW
     */
    public List<ConsultDoc> searchByPriority(String priority, String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            b.filter(f -> f.term(t -> t.field("priority").value(priority)));
                            if (keyword != null && !keyword.isBlank()) {
                                b.must(m -> m
                                        .multiMatch(mm -> mm
                                                .fields(FULL_FIELDS)
                                                .query(keyword)
                                                .fuzziness("AUTO")
                                        )
                                );
                            }
                            return b;
                        })
                )
                .withSort(Sort.by(Sort.Direction.DESC, "riskScore"))
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    // ── 분석 전용 메서드 ────────────────────────────────────────────────────────

    /**
     * 응대품질 분석: 인삿말·마무리 인사 여부로 상담 필터링.
     *
     * <ul>
     *   <li>{@code hasGreeting=false} → 인삿말 없는 상담 (품질 미달 후보)</li>
     *   <li>{@code hasFarewell=false} → 마무리 인사 없는 상담 (품질 미달 후보)</li>
     *   <li>null 전달 시 해당 조건 미적용</li>
     * </ul>
     */
    public List<ConsultDoc> searchByGreetingFlag(
            Boolean hasGreeting, Boolean hasFarewell, String keyword, int page, int size) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            if (hasGreeting != null) {
                                b.filter(f -> f.term(t -> t.field("hasGreeting").value(hasGreeting)));
                            }
                            if (hasFarewell != null) {
                                b.filter(f -> f.term(t -> t.field("hasFarewell").value(hasFarewell)));
                            }
                            if (keyword != null && !keyword.isBlank()) {
                                b.must(m -> m.multiMatch(mm -> mm
                                        .fields("rawText.analysis")
                                        .query(keyword)
                                        .fuzziness("AUTO")
                                ));
                            }
                            return b;
                        })
                )
                .withSort(Sort.by(Sort.Direction.DESC, "riskScore"))
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * 분석용 키워드 검색: {@code allText.analysis} 서브필드 대상.
     * 인삿말·응대 어근이 제거된 토큰으로 검색하여 실질 내용 관련 문서를 반환.
     */
    public List<ConsultDoc> searchByAnalysisKeyword(String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("allText.analysis^2.0", "iamIssue^3.0", "iamAction^2.0")
                                .query(keyword)
                                .fuzziness("AUTO")
                        )
                )
                .withPageable(PageRequest.of(page, size))
                .build();

        return toList(elasticsearchOperations.search(query, ConsultDoc.class));
    }

    /**
     * IAM 필드 기반 검색 추천어 (Elasticsearch match_phrase_prefix).
     *
     * <p>동의어 사전·형태소 분석이 적용된 {@code korean_search_analyzer}로 검색하므로
     * 구어체·축약어 입력도 정규 표현으로 확장되어 추천어가 반환됩니다.</p>
     *
     * @param prefix 입력 중인 검색어 (빈 값이면 빈 리스트 반환)
     * @param field  대상 필드: {@code iamIssue} / {@code iamAction} / {@code iamMemo} / {@code all}
     * @param limit  최대 반환 개수
     */
    public List<String> suggestIamKeywords(String prefix, String field, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();

        List<String> targetFields = switch (field) {
            case "iamIssue"  -> List.of("iamIssue");
            case "iamAction" -> List.of("iamAction");
            case "iamMemo"   -> List.of("iamMemo");
            default          -> List.of("iamIssue", "iamAction", "iamMemo");
        };

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            targetFields.forEach(f -> b.should(s -> s
                                    .matchPhrasePrefix(mp -> mp.field(f).query(prefix))
                            ));
                            b.minimumShouldMatch("1");
                            return b;
                        })
                )
                .withPageable(PageRequest.of(0, limit * 3))
                .build();

        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        for (ConsultDoc doc : toList(elasticsearchOperations.search(query, ConsultDoc.class))) {
            if (suggestions.size() >= limit) break;
            for (String f : targetFields) {
                String value = extractField(doc, f);
                if (value != null && !value.isBlank()) {
                    suggestions.add(value.trim());
                }
            }
        }
        return new ArrayList<>(suggestions).subList(0, Math.min(limit, suggestions.size()));
    }

    private String extractField(ConsultDoc doc, String field) {
        return switch (field) {
            case "iamIssue"  -> doc.getIamIssue();
            case "iamAction" -> doc.getIamAction();
            case "iamMemo"   -> doc.getIamMemo();
            default -> null;
        };
    }

    private List<ConsultDoc> toList(SearchHits<ConsultDoc> hits) {
        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
