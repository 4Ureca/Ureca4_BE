package com.uplus.crm.domain.summary.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.document.es.ConsultSearchDocument;
import com.uplus.crm.domain.summary.dto.request.ConsultationSearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDto;
import com.uplus.crm.domain.summary.repository.ConsultationMongoRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ConsultationSearchService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final ConsultationMongoRepository consultationMongoRepository;

  public Page<ConsultationSummaryDto> search(ConsultationSearchRequest request, Pageable pageable) {
    NativeQuery query = buildQuery(request, pageable);

    SearchHits<ConsultSearchDocument> searchHits =
        elasticsearchOperations.search(query, ConsultSearchDocument.class);

    List<Long> consultIds = searchHits.getSearchHits().stream()
        .map(SearchHit::getContent)
        .map(ConsultSearchDocument::getConsultId)
        .filter(StringUtils::hasText)
        .map(this::toLongSafely)
        .filter(java.util.Objects::nonNull)
        .toList();

    if (consultIds.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, searchHits.getTotalHits());
    }

    List<ConsultationSummary> summaries = consultationMongoRepository.findByConsultIdIn(consultIds);
    Map<Long, ConsultationSummary> summaryMap = new LinkedHashMap<>();
    for (ConsultationSummary summary : summaries) {
      summaryMap.put(summary.getConsultId(), summary);
    }

    List<ConsultationSummaryDto> content = consultIds.stream()
        .map(summaryMap::get)
        .filter(java.util.Objects::nonNull)
        .map(ConsultationSummaryDto::from)
        .toList();

    return new PageImpl<>(content, pageable, searchHits.getTotalHits());
  }

  private NativeQuery buildQuery(ConsultationSearchRequest request, Pageable pageable) {
    BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
    List<Query> filters = new ArrayList<>();

    if (StringUtils.hasText(request.getKeyword())) {
      boolBuilder.must(buildKeywordQuery(request.getKeyword()));
    }

    addTermFilter(filters, "agentId", request.getAgentId());
    addTermFilter(filters, "categoryCode", request.getCategoryCode());
    addTermFilter(filters, "categoryLarge", request.getCategoryLarge());
    addTermFilter(filters, "categoryMedium", request.getCategoryMedium());
    addTermFilter(filters, "categorySmall", request.getCategorySmall());
    addTermFilter(filters, "grade", request.getGrade());
    addTermFilter(filters, "gender", request.getGender());
    addTermFilter(filters, "channel", request.getChannel());
    addTermFilter(filters, "customerId", request.getCustomerId());
    addTermFilter(filters, "intent", request.getIntent());
    addTermFilter(filters, "defenseAttempted", request.getDefenseAttempted());
    addTermFilter(filters, "defenseSuccess", request.getDefenseSuccess());
    addPartialMatchFilter(filters, "agentName", request.getAgentName());
    addPartialMatchFilter(filters, "customerName", request.getCustomerName());
    addPartialMatchFilter(filters, "phone", request.getCustomerPhone());
    addAndTermsFilter(filters, "products", request.getProductCode());
    addNestedRiskFilter(filters, request.getRiskType(), request.getRiskLevel());

    if (request.getFromDate() != null || request.getToDate() != null) {
      LocalDateTime from = request.getFromDate() != null ? request.getFromDate().atStartOfDay() : null;
      LocalDateTime to = request.getToDate() != null ? request.getToDate().atTime(23, 59, 59) : null;

      filters.add(Query.of(q -> q.range(r -> r.date(d -> d
          .field("consultedAt")
           .gte(from != null ? toEsDateTimeString(from) : null)
          .lte(to != null ? toEsDateTimeString(to) : null)))));
    }

    if (request.getMinDuration() != null || request.getMaxDuration() != null) {
      filters.add(Query.of(q -> q.range(r -> r.number(n -> n
          .field("durationSec")
          .gte(request.getMinDuration() != null ? request.getMinDuration().doubleValue() : null)
          .lte(request.getMaxDuration() != null ? request.getMaxDuration().doubleValue() : null)))));
    }

    if (!filters.isEmpty()) {
      boolBuilder.filter(filters);
    }

    NativeQueryBuilderWrapper queryBuilder = new NativeQueryBuilderWrapper(
        NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
            .withPageable(pageable)
    );

    if (pageable.getSort().isSorted()) {
      for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
        queryBuilder = queryBuilder.withSort(order.getProperty(),
            order.isAscending() ? SortOrder.Asc : SortOrder.Desc);
      }
    } else {
      queryBuilder = queryBuilder.withSort("consultedAt", SortOrder.Desc);
    }

    return queryBuilder.build();
  }

  private Query buildKeywordQuery(String keyword) {
    List<String> phrases = splitTerms(keyword);

    if (CollectionUtils.isEmpty(phrases)) {
      return Query.of(q -> q.matchAll(m -> m));
    }

    return Query.of(q -> q.bool(b -> {
      phrases.forEach(phrase -> b.should(buildKeywordPhraseQuery(phrase)));
      return b.minimumShouldMatch("1");
    }));
  }

  private Query buildKeywordPhraseQuery(String phrase) {
    return Query.of(q -> q.bool(b -> b
        .minimumShouldMatch("1")
        .should(s -> s.multiMatch(m -> m
            .query(phrase)
            .fields("allText", "customerName")
        ))
        .should(s -> s.matchPhrase(mp -> mp.field("agentName").query(phrase)))
        .should(s -> s.matchPhrase(mp -> mp.field("phone").query(phrase)))
    ));
  }

  private Long toLongSafely(String value) {
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private void addTermFilter(List<Query> filters, String field, String value) {
    if (StringUtils.hasText(value)) {
      addExactStringTermFilter(filters, field, value);
    }
  }

  private void addTermFilter(List<Query> filters, String field, Boolean value) {
    if (value != null) {
      filters.add(Query.of(q -> q.term(t -> t.field(field).value(value))));
    }
  }


  private void addExactStringTermFilter(List<Query> filters, String field, String value) {
    filters.add(Query.of(q -> q.bool(b -> b
        .minimumShouldMatch("1")
        .should(s -> s.term(t -> t.field(field).value(value)))
        .should(s -> s.term(t -> t.field(field + ".keyword").value(value)))
    )));
  }

  private Query buildExactStringTermShouldClause(String field, String value) {
    return Query.of(q -> q.bool(b -> b
        .minimumShouldMatch("1")
        .should(s -> s.term(t -> t.field(field).value(value)))
        .should(s -> s.term(t -> t.field(field + ".keyword").value(value)))
    ));
  }

  private void addPartialMatchFilter(List<Query> filters, String field, String value) {
    if (!StringUtils.hasText(value)) {
      return;
    }

    String wildcardValue = "*" + value.trim() + "*";
    filters.add(Query.of(q -> q.bool(b -> b
        .minimumShouldMatch("1")
        .should(s -> s.wildcard(w -> w.field(field).value(wildcardValue)))
        .should(s -> s.wildcard(w -> w.field(field + ".keyword").value(wildcardValue)))
    )));
  }

  private void addAndTermsFilter(List<Query> filters, String field, List<String> values) {
    splitTerms(values)
        .forEach(item -> addExactStringTermFilter(filters, field, item));
  }

  private void addNestedRiskFilter(List<Query> filters, List<String> riskTypes, List<String> riskLevels) {
    List<String> types = splitTerms(riskTypes);
    List<String> levels = splitTerms(riskLevels);

    if (types.isEmpty() && levels.isEmpty()) {
      return;
    }

    types.forEach(type ->
        filters.add(Query.of(q -> q.term(t -> t
            .field("riskFlags.riskType").value(type))))
    );

    if (!levels.isEmpty()) {
      filters.add(Query.of(q -> q.bool(b -> {
        levels.forEach(level -> b.should(s -> s.term(t -> t
            .field("riskFlags.riskLevel").value(level))));
        return b.minimumShouldMatch("1");
      })));
    }
  }
  
  private List<String> splitTerms(String value) {
    if (!StringUtils.hasText(value)) {
      return List.of();
    }

    return Arrays.stream(value.trim().split("\\s+"))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .distinct()
        .toList();
  }

  private List<String> splitTerms(List<String> values) {
    if (CollectionUtils.isEmpty(values)) {
      return List.of();
    }

    return values.stream()
        .filter(StringUtils::hasText)
        .map(String::trim)
        .filter(StringUtils::hasText)
        .distinct()
        .toList();
  }

  private String toEsDateTimeString(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  private record NativeQueryBuilderWrapper(NativeQueryBuilder builder) {

    private NativeQueryBuilderWrapper withSort(String field, SortOrder order) {
      return new NativeQueryBuilderWrapper(builder.withSort(s -> s.field(f -> f
          .field(field)
          .order(order))));
    }

    private NativeQuery build() {
      return builder.build();
    }
  }
}
