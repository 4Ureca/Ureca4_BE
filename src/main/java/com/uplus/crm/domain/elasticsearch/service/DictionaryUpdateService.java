package com.uplus.crm.domain.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch 사용자 사전(Custom Dictionary) 업데이트 서비스.
 *
 * <h3>전체 프로세스</h3>
 * <ol>
 *   <li>MongoDB {@code consultation_summary} 에서 키워드 추출
 *       ({@code summary.keywords}, {@code iam.matchKeyword} 집계)</li>
 *   <li>(선택) 추출된 키워드를 로컬 사전 파일에 병합 기록</li>
 *   <li>ES {@code _reload_search_analyzers} API 호출 → 새 어휘 즉시 반영</li>
 * </ol>
 *
 * <h3>Docker 볼륨 설정 (사전 파일 반영)</h3>
 * <pre>
 * # docker-compose.yml
 * volumes:
 *   - ./es-analysis:/usr/share/elasticsearch/config/analysis
 * </pre>
 * {@code dictionary.output-path} 를 {@code ./es-analysis/userdict.txt} 로 설정하면
 * 스프링 앱이 파일을 쓰고 ES가 해당 경로를 읽는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryUpdateService {

    private static final String INDEX_NAME = "consult-index";
    private static final int DEFAULT_LIMIT = 500;

    private final MongoTemplate mongoTemplate;
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 사전 파일 출력 경로 (ES 볼륨 마운트 경로와 동일하게 설정).
     * 미설정 시 파일 기록 단계를 건너뛰고 키워드 목록만 반환.
     */
    @Value("${dictionary.output-path:}")
    private String outputPath;

    // ── 공개 API ─────────────────────────────────────────────────────────

    /**
     * 사전 전체 업데이트 파이프라인 실행.
     *
     * <ol>
     *   <li>MongoDB에서 키워드 추출</li>
     *   <li>사전 파일 기록 (outputPath 설정된 경우)</li>
     *   <li>ES 분석기 리로드</li>
     * </ol>
     *
     * @return 추출된 키워드 목록 (빈도 내림차순)
     */
    public List<KeywordEntry> runUpdate() {
        List<KeywordEntry> keywords = extractKeywords(DEFAULT_LIMIT);
        log.info("[DictionaryUpdate] 추출된 키워드 {}건", keywords.size());

        if (!outputPath.isBlank()) {
            writeToDictionaryFile(keywords);
        } else {
            log.info("[DictionaryUpdate] dictionary.output-path 미설정 — 파일 기록 건너뜀");
        }

        reloadAnalyzers();
        return keywords;
    }

    /**
     * MongoDB에서 주요 키워드를 추출하여 빈도 내림차순 반환.
     *
     * <ul>
     *   <li>{@code summary.keywords}: AI 추출 상담 키워드 (주 소스)</li>
     *   <li>{@code iam.matchKeyword}: IAM 매칭 키워드 (보조 소스)</li>
     * </ul>
     *
     * @param limit 최대 반환 건수
     */
    public List<KeywordEntry> extractKeywords(int limit) {
        Map<String, Long> merged = new LinkedHashMap<>();

        // 1. summary.keywords (AI 추출 — 품질 높음)
        aggregateField("summary.keywords", limit).forEach(
                e -> merged.put(e.keyword(), e.count()));

        // 2. iam.matchKeyword (IAM 매칭 — 보완)
        aggregateField("iam.matchKeyword", limit / 2).forEach(
                e -> merged.merge(e.keyword(), e.count(), Long::sum));

        return merged.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new KeywordEntry(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * ES {@code _reload_search_analyzers} API 호출.
     * 사전 파일이 갱신된 후 호출하면 새 어휘가 즉시 반영된다.
     *
     * <p>주의: 파일 자체는 이 메서드가 변경하지 않는다.
     * {@link #runUpdate()} 또는 {@link #writeToDictionaryFile(List)} 를 먼저 호출하라.</p>
     */
    public void reloadAnalyzers() {
        try {
            var response = elasticsearchClient.indices()
                    .reloadSearchAnalyzers(r -> r.index(INDEX_NAME));
            log.info("[DictionaryUpdate] _reload_search_analyzers 완료: index={}, shards={}",
                    INDEX_NAME, response.reloadDetails().size());
        } catch (IOException e) {
            log.error("[DictionaryUpdate] 분석기 리로드 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("ES 분석기 리로드 실패: " + e.getMessage(), e);
        }
    }

    // ── private ───────────────────────────────────────────────────────────

    /**
     * MongoDB 배열 필드를 unwind → group → sort → limit 집계.
     * 반환 형식: [{keyword, count}, ...]
     */
    private List<KeywordEntry> aggregateField(String field, int n) {
        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(Aggregation.match(Criteria.where(field).exists(true).ne(null)));
        ops.add(Aggregation.unwind("$" + field));
        // 빈 문자열·null 제거
        ops.add(Aggregation.match(Criteria.where(field).ne("").ne(null)));
        ops.add(Aggregation.group(field).count().as("cnt"));
        ops.add(Aggregation.sort(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "cnt")));
        ops.add(Aggregation.limit(n));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(ops),
                        ConsultationSummary.class, Document.class)
                .getMappedResults().stream()
                .map(d -> {
                    String kw = d.getString("_id");
                    Number cnt = (Number) d.get("cnt");
                    return (kw != null && !kw.isBlank())
                            ? new KeywordEntry(kw, cnt != null ? cnt.longValue() : 0L)
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 추출된 키워드를 userdict.txt 형식으로 파일에 기록(병합).
     * ES Nori 사전 형식: {@code 단어 [품사태그]}
     * 기존 파일 내용은 보존하고 신규 항목만 추가한다.
     */
    private void writeToDictionaryFile(List<KeywordEntry> keywords) {
        Path path = Path.of(outputPath);
        try {
            // 기존 파일 읽기
            List<String> existing = Files.exists(path)
                    ? Files.readAllLines(path, StandardCharsets.UTF_8)
                    : List.of();

            List<String> newLines = keywords.stream()
                    .map(KeywordEntry::keyword)
                    .filter(kw -> existing.stream().noneMatch(line -> line.startsWith(kw)))
                    .toList();

            if (newLines.isEmpty()) {
                log.info("[DictionaryUpdate] 새로 추가할 사전 항목 없음");
                return;
            }

            Files.write(path, newLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            log.info("[DictionaryUpdate] 사전 파일 {}건 추가 기록: {}", newLines.size(), path);

        } catch (IOException e) {
            log.error("[DictionaryUpdate] 사전 파일 기록 실패: {}", e.getMessage(), e);
        }
    }

    // ── 내부 DTO ──────────────────────────────────────────────────────────

    /**
     * 키워드와 출현 빈도.
     *
     * @param keyword 키워드 문자열
     * @param count   MongoDB 내 출현 횟수
     */
    public record KeywordEntry(String keyword, long count) {}
}
