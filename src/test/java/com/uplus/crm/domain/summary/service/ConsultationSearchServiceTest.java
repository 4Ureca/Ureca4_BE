package com.uplus.crm.domain.summary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.uplus.crm.domain.summary.document.es.ConsultSearchDocument;
import com.uplus.crm.domain.summary.dto.request.ConsultationSearchRequest;
import com.uplus.crm.domain.summary.repository.ConsultationMongoRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

@ExtendWith(MockitoExtension.class)
class ConsultationSearchServiceTest {

  @InjectMocks
  private ConsultationSearchService consultationSearchService;

  @Mock
  private ElasticsearchOperations elasticsearchOperations;

  @Mock
  private ConsultationMongoRepository consultationMongoRepository;

  @Test
  @DisplayName("search - 복합 검색 필터 생성")
  void search_buildsQueryFromRequest() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);
    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setKeyword("해지 방어");
    request.setIntent(true);
    request.setDefenseAttempted(true);
    request.setDefenseSuccess(false);
    request.setRiskType(List.of("R1", "R2"));
    request.setRiskLevel(List.of("L1", "L2"));
    request.setProductCode(List.of("P1", "P2"));
    request.setAgentName("홍길동");
    request.setCustomerName("김고객");
    request.setCustomerPhone("010-1234");

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("intent");
    assertThat(queryString).contains("defenseAttempted");
    assertThat(queryString).contains("defenseSuccess");
    assertThat(queryString).contains("riskFlags.riskType");
    assertThat(queryString).contains("riskFlags.riskLevel");
    assertThat(queryString).contains("products");
    assertThat(queryString).contains("agentName");
    assertThat(queryString).contains("customerName");
    assertThat(queryString).contains("phone");
  }

  @Test
  @DisplayName("search - productCode 리스트 필터 생성")
  void search_productCodeListFilter() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setProductCode(List.of("MOB-NGT-07", "MOB-5G-ST", "TEL-HOME-UNL"));

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("products");
    assertThat(queryString).contains("products.keyword");
  }

  @Test
  @DisplayName("search - 날짜 범위 필터 생성")
  void search_dateRangeFilter() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setFromDate(LocalDate.of(2026, 3, 10));
    request.setToDate(LocalDate.of(2026, 3, 10));

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("2026-03-10T00:00:00");
    assertThat(queryString).contains("2026-03-10T23:59:59");
  }

  @Test
  @DisplayName("search - duration 범위 필터 생성")
  void search_durationRangeFilter() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setMinDuration(10);
    request.setMaxDuration(120);

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("durationSec");
  }

  @Test
  @DisplayName("search - agentName wildcard 필터 생성")
  void search_agentNameWildcardFilter() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setAgentName("홍");

    consultationSearchService.search(request, PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    String queryString = String.valueOf(queryCaptor.getValue().getQuery());

    assertThat(queryString).contains("agentName");
    assertThat(queryString).contains("*홍*");
  }

  @Test
  @DisplayName("search - 기본 정렬 consultedAt DESC 적용")
  void search_defaultSort() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    consultationSearchService.search(new ConsultationSearchRequest(), PageRequest.of(0, 20));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    assertThat(queryCaptor.getValue().getSortOptions()).hasSize(1);
    assertThat(queryCaptor.getValue().getSortOptions().get(0).field().field())
        .isEqualTo("consultedAt");
  }

  @Test
  @DisplayName("search - pageable 정렬 적용")
  void search_pageableSort() {

    SearchHits<ConsultSearchDocument> emptyHits = mock(SearchHits.class);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(emptyHits);

    ConsultationSearchRequest request = new ConsultationSearchRequest();
    request.setKeyword("해지");

    consultationSearchService.search(
        request,
        PageRequest.of(0, 20,
            org.springframework.data.domain.Sort.by("durationSec")));

    ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

    org.mockito.Mockito.verify(elasticsearchOperations)
        .search(queryCaptor.capture(), any(Class.class));

    assertThat(queryCaptor.getValue().getSortOptions()).hasSize(1);
    assertThat(queryCaptor.getValue().getSortOptions().get(0).field().field())
        .isEqualTo("durationSec");
  }

  @Test
  @DisplayName("search - ES 결과 없으면 빈 페이지 반환")
  void search_emptyEsResult() {

    SearchHits<ConsultSearchDocument> hits = mock(SearchHits.class);
    given(hits.getSearchHits()).willReturn(List.of());
    given(hits.getTotalHits()).willReturn(0L);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(hits);

    Page<?> result = consultationSearchService.search(
        new ConsultationSearchRequest(),
        PageRequest.of(0, 20));

    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("search - Mongo 결과 없으면 DTO 생성되지 않는다")
  void search_missingMongoSummary() {

    ConsultSearchDocument doc = new ConsultSearchDocument();
    doc.setConsultId("100");

    SearchHit<ConsultSearchDocument> hit = mock(SearchHit.class);
    given(hit.getContent()).willReturn(doc);

    SearchHits<ConsultSearchDocument> hits = mock(SearchHits.class);
    given(hits.getSearchHits()).willReturn(List.of(hit));
    given(hits.getTotalHits()).willReturn(1L);

    given(elasticsearchOperations.search(any(NativeQuery.class), any(Class.class)))
        .willReturn(hits);

    given(consultationMongoRepository.findByConsultIdIn(any()))
        .willReturn(List.of());

    Page<?> result = consultationSearchService.search(
        new ConsultationSearchRequest(),
        PageRequest.of(0, 20));

    assertThat(result.getContent()).isEmpty();
  }
}