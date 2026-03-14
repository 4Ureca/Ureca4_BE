package com.uplus.crm.domain.consultation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.consultation.dto.response.ConsultationDetailResponseDto;
import com.uplus.crm.domain.consultation.repository.ConsultationDetailQueryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ConsultationDetailServiceTest {

    @InjectMocks
    private ConsultationDetailService consultationDetailService;

    @Mock private ConsultationDetailQueryRepository consultationDetailQueryRepository;
    @Mock private ConsultationRawTextRepository consultationRawTextRepository;
    @Mock private CustomerRepository customerRepository;

    @Test
    @DisplayName("상담 상세 조회 성공 - 모든 영역의 데이터가 정상적으로 조합되어야 한다")
    void getConsultationDetail_Success() {
        // given
        Long consultId = 1L;
        ConsultationDetailQueryRepository.BasicInfoRow basicRow = mock(ConsultationDetailQueryRepository.BasicInfoRow.class);
        
        given(basicRow.customerId()).willReturn(100L);
        given(basicRow.customerName()).willReturn("홍길동");
        given(consultationDetailQueryRepository.findBasicInfo(consultId)).willReturn(basicRow);
        given(customerRepository.findActiveSubscribedProducts(100L)).willReturn(List.of());
        given(consultationRawTextRepository.findFirstByConsultId(consultId)).willReturn(Optional.empty());
        given(consultationDetailQueryRepository.findHistory(consultId)).willReturn(List.of());

        // when
        ConsultationDetailResponseDto result = consultationDetailService.getConsultationDetail(consultId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBasicInfo().getCustomerName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("상담 정보가 존재하지 않을 때 BusinessException을 던져야 한다")
    void getConsultationDetail_NotFound_ThrowsException() {
        // given
        given(consultationDetailQueryRepository.findBasicInfo(anyLong())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> consultationDetailService.getConsultationDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONSULTATION_NOT_FOUND);
    }
}