package com.uplus.crm.domain.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.repository.ConsultElasticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultSearchService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final List<String> FULL_FIELDS = List.of(
            "iamIssue^3.0",
            "iamAction^2.0",
            "content^1.5",
            "iamMemo",
            "allText",
            "customerName"
    );

    private final ConsultElasticRepository consultElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 상담 데이터 저장 (Indexing)
     */
    public void saveConsultation(ConsultDoc consultDoc) {
        consultElasticRepository.save(consultDoc);
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

    private List<ConsultDoc> toList(SearchHits<ConsultDoc> hits) {
        return hits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
