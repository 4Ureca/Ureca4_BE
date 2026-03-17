package com.uplus.crm.domain.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.demo.dto.request.DemoConsultSubmitRequest;
import com.uplus.crm.domain.demo.dto.response.DemoConsultDataResponse;
import com.uplus.crm.domain.demo.dto.response.DemoConsultSubmitResponse;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.demo.repository.DemoConsultationCategoryRepository;
import com.uplus.crm.domain.demo.repository.DemoConsultationResultRepository;
import com.uplus.crm.domain.demo.repository.DemoCustomerRepository;
import com.uplus.crm.domain.extraction.repository.ExcellentEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.ResultEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.SummaryEventStatusRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DemoConsultationServiceTest {

    @InjectMocks
    private DemoConsultationService demoConsultationService;

    @Mock private DemoConsultationResultRepository consultationResultRepository;
    @Mock private DemoCustomerRepository customerRepository;
    @Mock private DemoConsultationCategoryRepository categoryRepository;
    @Mock private ConsultationRawTextRepository rawTextRepository;
    @Mock private ResultEventStatusRepository resultEventStatusRepository;
    @Mock private ExcellentEventStatusRepository excellentEventStatusRepository;
    @Mock private SummaryEventStatusRepository summaryEventStatusRepository;

    // ── 픽스처 헬퍼 ─────────────────────────────────────────────────────────

    private ConsultationResult stubResult(Long consultId, Long customerId, String categoryCode) {
        return ConsultationResult.builder()
                .consultId(consultId)
                .empId(1)
                .customerId(customerId)
                .channel("CALL")
                .categoryCode(categoryCode)
                .durationSec(180)
                .build();
    }

    private Customer stubCustomer(Long customerId) {
        Customer customer = mock(Customer.class);
        given(customer.getCustomerId()).willReturn(customerId);
        given(customer.getName()).willReturn("홍길동");
        given(customer.getPhone()).willReturn("010-1234-5678");
        given(customer.getCustomerType()).willReturn("개인");
        given(customer.getGender()).willReturn("남성");
        given(customer.getBirthDate()).willReturn(LocalDate.of(1990, 1, 1));
        given(customer.getGradeCode()).willReturn("VIP");
        given(customer.getEmail()).willReturn("hong@example.com");
        return customer;
    }

    private ConsultationCategoryPolicy stubCategory(String code) {
        ConsultationCategoryPolicy category = mock(ConsultationCategoryPolicy.class);
        given(category.getCategoryCode()).willReturn(code);
        given(category.getLargeCategory()).willReturn("요금");
        given(category.getMediumCategory()).willReturn("청구");
        given(category.getSmallCategory()).willReturn("과금오류");
        return category;
    }

    // ── getRandomConsultData ─────────────────────────────────────────────────

    @Test
    @DisplayName("getRandomConsultData - 정상 조회 시 고객정보와 상담기본정보를 반환하고 IAM 3필드는 null이다")
    void getRandomConsultData_success_returnsDataWithNullIam() {
        ConsultationResult result = stubResult(10L, 1L, "CAT001");
        Customer customer = stubCustomer(1L);
        ConsultationCategoryPolicy category = stubCategory("CAT001");

        given(consultationResultRepository.findOneRandom()).willReturn(Optional.of(result));
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(categoryRepository.findById("CAT001")).willReturn(Optional.of(category));
        given(customerRepository.findActiveSubscribedProducts(1L)).willReturn(List.of());
        given(rawTextRepository.findFirstByConsultId(10L)).willReturn(Optional.empty());

        DemoConsultDataResponse response = demoConsultationService.getRandomConsultData();

        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.customerName()).isEqualTo("홍길동");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.channel()).isEqualTo("CALL");
        assertThat(response.categoryCode()).isEqualTo("CAT001");
        assertThat(response.largeCategory()).isEqualTo("요금");
        assertThat(response.durationSec()).isEqualTo(180);
        assertThat(response.subscribedProducts()).isEmpty();
        // IAM 필드는 반드시 null
        assertThat(response.iamIssue()).isNull();
        assertThat(response.iamAction()).isNull();
        assertThat(response.iamMemo()).isNull();
        // rawTextJson - 원문 없으면 null
        assertThat(response.rawTextJson()).isNull();
    }

    @Test
    @DisplayName("getRandomConsultData - consultation_results가 비어 있으면 CONSULTATION_NOT_FOUND 예외")
    void getRandomConsultData_noResult_throwsConsultationNotFound() {
        given(consultationResultRepository.findOneRandom()).willReturn(Optional.empty());

        assertThatThrownBy(() -> demoConsultationService.getRandomConsultData())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONSULTATION_NOT_FOUND));

        then(customerRepository).shouldHaveNoInteractions();
        then(categoryRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("getRandomConsultData - 랜덤 상담에 매핑된 고객이 없으면 CONSULTATION_NOT_FOUND 예외")
    void getRandomConsultData_customerMissing_throwsConsultationNotFound() {
        ConsultationResult result = stubResult(10L, 999L, "CAT001");

        given(consultationResultRepository.findOneRandom()).willReturn(Optional.of(result));
        given(customerRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> demoConsultationService.getRandomConsultData())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONSULTATION_NOT_FOUND));
    }

    @Test
    @DisplayName("getRandomConsultData - 카테고리 코드가 매핑되지 않으면 CONSULTATION_NOT_FOUND 예외")
    void getRandomConsultData_categoryMissing_throwsConsultationNotFound() {
        ConsultationResult result = stubResult(10L, 1L, "UNKNOWN");

        given(consultationResultRepository.findOneRandom()).willReturn(Optional.of(result));
        // getter 스텁 없이 bare mock 사용 — 이 경로에서 getter는 호출되지 않음
        given(customerRepository.findById(1L)).willReturn(Optional.of(mock(Customer.class)));
        given(categoryRepository.findById("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> demoConsultationService.getRandomConsultData())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONSULTATION_NOT_FOUND));
    }

    // ── submitConsult ────────────────────────────────────────────────────────

    @Test
    @DisplayName("submitConsult - 정상 저장 후 consultId와 createdAt을 반환한다")
    void submitConsult_success_returnsSavedIdAndCreatedAt() {
        DemoConsultSubmitRequest request = new DemoConsultSubmitRequest(
                1L, "CALL", "CAT001", 240, null,
                "고객이 요금 오류 제기", "시스템 확인 후 재청구", "추후 모니터링 필요", null
        );

        LocalDateTime now = LocalDateTime.now();
        ConsultationResult saved = ConsultationResult.builder()
                .consultId(99L)
                .empId(1)
                .customerId(1L)
                .channel("CALL")
                .categoryCode("CAT001")
                .durationSec(240)
                .iamIssue("고객이 요금 오류 제기")
                .iamAction("시스템 확인 후 재청구")
                .iamMemo("추후 모니터링 필요")
                .build();
        ReflectionTestUtils.setField(saved, "createdAt", now);

        given(consultationResultRepository.save(any(ConsultationResult.class))).willReturn(saved);

        DemoConsultSubmitResponse response = demoConsultationService.submitConsult(request, 1);

        assertThat(response.consultId()).isEqualTo(99L);
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("submitConsult - 저장 시 empId가 인증된 직원 ID로 설정된다")
    void submitConsult_setsEmpIdFromAuthentication() {
        DemoConsultSubmitRequest request = new DemoConsultSubmitRequest(
                2L, "CHATTING", "CAT002", 300, null, null, null, null, null
        );

        given(consultationResultRepository.save(any(ConsultationResult.class)))
                .willAnswer(inv -> {
                    ConsultationResult arg = inv.getArgument(0);
                    assertThat(arg.getEmpId()).isEqualTo(7);
                    return arg;
                });

        demoConsultationService.submitConsult(request, 7);

        then(consultationResultRepository).should().save(any(ConsultationResult.class));
    }
}
