package com.uplus.crm.domain.consultation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.consultation.dto.response.ConsultDataResponse;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationResultRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @InjectMocks
    private ConsultationService consultationService;

    @Mock private ConsultationResultRepository consultationResultRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ConsultationCategoryRepository categoryRepository;

    // ── 픽스처 헬퍼 ─────────────────────────────────────────────────────────

    private ConsultationResult stubResult(Long consultId, Long customerId, String categoryCode) {
        return ConsultationResult.builder()
                .consultId(consultId)
                .empId(1)
                .customerId(customerId)
                .channel("CALL")
                .categoryCode(categoryCode)
                .durationSec(180)
                .iamIssue("고객이 요금 오류 제기")
                .iamAction("시스템 확인 후 재청구")
                .iamMemo("추후 모니터링 필요")
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
    @DisplayName("getRandomConsultData - 정상 조회 시 IAM 3필드를 포함한 전체 상담 데이터를 반환한다")
    void getRandomConsultData_success_returnsDataWithIam() {
        ConsultationResult result = stubResult(10L, 1L, "CAT001");
        Customer customer = stubCustomer(1L);
        ConsultationCategoryPolicy category = stubCategory("CAT001");

        given(consultationResultRepository.findOneRandom()).willReturn(Optional.of(result));
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(categoryRepository.findById("CAT001")).willReturn(Optional.of(category));
        given(customerRepository.findActiveSubscribedProducts(1L)).willReturn(List.of());

        ConsultDataResponse response = consultationService.getRandomConsultData();

        assertThat(response.consultId()).isEqualTo(10L);
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.customerName()).isEqualTo("홍길동");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.channel()).isEqualTo("CALL");
        assertThat(response.categoryCode()).isEqualTo("CAT001");
        assertThat(response.largeCategory()).isEqualTo("요금");
        assertThat(response.durationSec()).isEqualTo(180);
        assertThat(response.subscribedProducts()).isEmpty();
        // IAM 필드는 DB 값을 그대로 반환
        assertThat(response.iamIssue()).isEqualTo("고객이 요금 오류 제기");
        assertThat(response.iamAction()).isEqualTo("시스템 확인 후 재청구");
        assertThat(response.iamMemo()).isEqualTo("추후 모니터링 필요");
    }

    @Test
    @DisplayName("getRandomConsultData - consultation_results가 비어 있으면 CONSULTATION_NOT_FOUND 예외")
    void getRandomConsultData_noResult_throwsConsultationNotFound() {
        given(consultationResultRepository.findOneRandom()).willReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.getRandomConsultData())
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

        assertThatThrownBy(() -> consultationService.getRandomConsultData())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONSULTATION_NOT_FOUND));
    }

    @Test
    @DisplayName("getRandomConsultData - 카테고리 코드가 매핑되지 않으면 CONSULTATION_NOT_FOUND 예외")
    void getRandomConsultData_categoryMissing_throwsConsultationNotFound() {
        ConsultationResult result = stubResult(10L, 1L, "UNKNOWN");

        given(consultationResultRepository.findOneRandom()).willReturn(Optional.of(result));
        given(customerRepository.findById(1L)).willReturn(Optional.of(mock(Customer.class)));
        given(categoryRepository.findById("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> consultationService.getRandomConsultData())
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CONSULTATION_NOT_FOUND));
    }
}
