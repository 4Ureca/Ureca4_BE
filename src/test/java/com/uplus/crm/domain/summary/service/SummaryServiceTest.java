package com.uplus.crm.domain.summary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.request.SummarySearchRequest;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    // ── MongoDB ───────────────────────────────────────────────────────────
    @Mock private SummaryRepository summaryRepository;
    @Mock private MongoTemplate mongoTemplate;

    // ── RDB ───────────────────────────────────────────────────────────────
    @Mock private SummaryConsultationResultRepository consultationResultRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ConsultationRawTextRepository rawTextRepository;
    @Mock private ConsultationCategoryRepository categoryRepository;
    @Mock private EmployeeRepository employeeRepository;

    // ── ES ────────────────────────────────────────────────────────────────
    @Mock private ConsultSearchService consultSearchService;

    @InjectMocks
    private SummaryService summaryService;

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private ConsultationResult makeResult(Long consultId) {
        return ConsultationResult.builder()
                .consultId(consultId)
                .empId(1)
                .customerId(10L)
                .channel("CALL")
                .categoryCode("M_FEE_01")
                .durationSec(300)
                .build();
    }

    private ConsultationSummary makeSummary(Long consultId) {
        ConsultationSummary s = new ConsultationSummary();
        s.setConsultId(consultId);
        s.setConsultedAt(LocalDateTime.now());
        s.setChannel("CALL");
        s.setDurationSec(300);
        return s;
    }

    // ── 목록 조회 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("목록 조회: keyword 없으면 MongoDB 전체 검색 (ES 미호출)")
    void search_noKeyword_returnsMongoPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "consultedAt"));
        ConsultationSummary entity = makeSummary(1L);

        given(mongoTemplate.count(any(Query.class), any(Class.class))).willReturn(1L);
        given(mongoTemplate.find(any(Query.class), any(Class.class))).willReturn(List.of(entity));

        Page<ConsultationSummaryListResponse> result =
                summaryService.search(new SummarySearchRequest(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        then(consultSearchService).should(never()).searchWithPriority(any());
    }

    @Test
    @DisplayName("목록 조회: AND 결과(해지) + OR-only 결과 → AND 우선 페이지 조립")
    void search_keywordWithEsIds_andFirstOrSecond() {
        Pageable pageable = PageRequest.of(0, 10);
        SummarySearchRequest req = new SummarySearchRequest();
        req.setKeyword("해지");

        // AND: consultId 1,2 / OR-only: consultId 3,4
        var esResult = new ConsultSearchService.KeywordSearchResult(
                List.of(1L, 2L), List.of(3L, 4L));
        given(consultSearchService.searchWithPriority("해지")).willReturn(esResult);
        given(mongoTemplate.count(any(Query.class), any(Class.class))).willReturn(2L);
        given(mongoTemplate.find(any(Query.class), any(Class.class))).willReturn(List.of());

        summaryService.search(req, pageable);

        then(consultSearchService).should().searchWithPriority("해지");
    }

    @Test
    @DisplayName("목록 조회: ES 결과 없으면 MongoDB regex fallback")
    void search_keywordEsEmpty_mongoFallback() {
        Pageable pageable = PageRequest.of(0, 10);
        SummarySearchRequest req = new SummarySearchRequest();
        req.setKeyword("해지");

        var emptyResult = new ConsultSearchService.KeywordSearchResult(List.of(), List.of());
        given(consultSearchService.searchWithPriority("해지")).willReturn(emptyResult);
        given(mongoTemplate.count(any(Query.class), any(Class.class))).willReturn(0L);
        given(mongoTemplate.find(any(Query.class), any(Class.class))).willReturn(List.of());

        summaryService.search(req, pageable);

        // ES 호출했지만 빈 결과 → MongoDB regex fallback (예외 없이 정상 동작)
        then(consultSearchService).should().searchWithPriority("해지");
    }

    // ── 상세 조회 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("상세 조회: RDB + MongoDB 정상 병합")
    void getDetail_success_mergesRdbAndMongo() {
        Long consultId = 1L;
        ConsultationResult result = makeResult(consultId);
        ConsultationSummary summary = makeSummary(consultId);

        given(consultationResultRepository.findById(consultId)).willReturn(Optional.of(result));
        given(summaryRepository.findByConsultId(consultId)).willReturn(Optional.of(summary));
        given(customerRepository.findById(10L)).willReturn(Optional.empty());
        given(rawTextRepository.findFirstByConsultId(consultId)).willReturn(Optional.empty());
        given(customerRepository.findActiveSubscribedProducts(10L)).willReturn(List.of());
        given(categoryRepository.findById("M_FEE_01")).willReturn(Optional.empty());
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.empty());

        ConsultationSummaryDetailResponse response = summaryService.getDetail(consultId);

        assertThat(response.getConsultId()).isEqualTo(consultId);
        assertThat(response.getDurationSec()).isEqualTo(300);
        then(consultationResultRepository).should().findById(consultId);
        then(summaryRepository).should().findByConsultId(consultId);
    }

    @Test
    @DisplayName("상세 조회: RDB 없으면 404")
    void getDetail_rdbNotFound_throws404() {
        Long consultId = 99L;
        given(consultationResultRepository.findById(consultId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> summaryService.getDetail(consultId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);

        then(summaryRepository).should(never()).findByConsultId(any());
    }

    @Test
    @DisplayName("상세 조회: MongoDB 없어도 RDB 데이터로 부분 응답 (404 아님)")
    void getDetail_noMongoDB_returnsPartialResponse() {
        Long consultId = 2L;
        ConsultationResult result = makeResult(consultId);

        given(consultationResultRepository.findById(consultId)).willReturn(Optional.of(result));
        given(summaryRepository.findByConsultId(consultId)).willReturn(Optional.empty());
        given(customerRepository.findById(10L)).willReturn(Optional.empty());
        given(rawTextRepository.findFirstByConsultId(consultId)).willReturn(Optional.empty());
        given(customerRepository.findActiveSubscribedProducts(10L)).willReturn(List.of());
        given(categoryRepository.findById("M_FEE_01")).willReturn(Optional.empty());
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.empty());

        // 예외 없이 부분 응답 반환
        ConsultationSummaryDetailResponse response = summaryService.getDetail(consultId);

        assertThat(response.getConsultId()).isEqualTo(consultId);
        assertThat(response.getContent().getAiSummary()).isNull();
        assertThat(response.getActiveSubscriptions()).isEmpty();
    }

    @Test
    @DisplayName("상세 조회: 가입 상품 목록이 있으면 응답에 포함")
    void getDetail_withActiveProducts_included() {
        Long consultId = 3L;
        ConsultationResult result = makeResult(consultId);

        CustomerRepository.SubscribedProductProjection home =
                new CustomerRepository.SubscribedProductProjection() {
                    public String getProductType() { return "HOME"; }
                    public String getProductCode() { return "GIGA_SLIM"; }
                    public String getProductName() { return "기가슬림"; }
                    public String getCategory() { return "인터넷"; }
                };

        given(consultationResultRepository.findById(consultId)).willReturn(Optional.of(result));
        given(summaryRepository.findByConsultId(consultId)).willReturn(Optional.empty());
        given(customerRepository.findById(10L)).willReturn(Optional.empty());
        given(rawTextRepository.findFirstByConsultId(consultId)).willReturn(Optional.empty());
        given(customerRepository.findActiveSubscribedProducts(10L)).willReturn(List.of(home));
        given(categoryRepository.findById("M_FEE_01")).willReturn(Optional.empty());
        given(employeeRepository.findByIdWithDetails(1)).willReturn(Optional.empty());

        ConsultationSummaryDetailResponse response = summaryService.getDetail(consultId);

        assertThat(response.getActiveSubscriptions()).hasSize(1);
        assertThat(response.getActiveSubscriptions().get(0).getProductName()).isEqualTo("기가슬림");
    }
}
