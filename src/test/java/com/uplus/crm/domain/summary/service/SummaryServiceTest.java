package com.uplus.crm.domain.summary.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
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
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

  @Mock
  private SummaryRepository summaryRepository;

  @Mock
  private SummaryConsultationResultRepository consultationResultRepository;

  @InjectMocks
  private SummaryService summaryService;

  @Test
  @DisplayName("목록 조회 성공")
  void getList_success() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "consultedAt"));

    ConsultationSummary entity = ConsultationSummary.builder()
        .consultId(1L)
        .consultedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .channel("CALL")
        .durationSec(300)
        .build();

    Page<ConsultationSummary> page =
        new PageImpl<>(List.of(entity), pageable, 1);

    given(summaryRepository.findAll(pageable)).willReturn(page);

    Page<?> result = summaryService.getList(pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    then(summaryRepository).should().findAll(pageable);
  }

  @Test
  @DisplayName("상세 조회 성공")
  void getDetail_success() {
    Long consultId = 1L;

    ConsultationSummary entity = ConsultationSummary.builder()
        .consultId(consultId)
        .consultedAt(LocalDateTime.now())
        .createdAt(LocalDateTime.now())
        .channel("CALL")
        .durationSec(200)
        .build();

    given(consultationResultRepository.existsById(consultId)).willReturn(true);
    given(summaryRepository.findByConsultId(consultId))
        .willReturn(Optional.of(entity));

    var response = summaryService.getDetail(consultId);

    assertThat(response.getConsultId()).isEqualTo(consultId);
    then(consultationResultRepository).should().existsById(consultId);
    then(summaryRepository).should().findByConsultId(consultId);
  }

  @Test
  @DisplayName("상담결과가 존재하지 않으면 예외")
  void getDetail_consultation_not_found() {
    Long consultId = 1L;

    given(consultationResultRepository.existsById(consultId))
        .willReturn(false);

    assertThatThrownBy(() -> summaryService.getDetail(consultId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);

    then(summaryRepository).should(never()).findByConsultId(any());
  }

  @Test
  @DisplayName("요약이 존재하지 않으면 예외")
  void getDetail_summary_not_found() {
    Long consultId = 1L;

    given(consultationResultRepository.existsById(consultId))
        .willReturn(true);

    given(summaryRepository.findByConsultId(consultId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> summaryService.getDetail(consultId))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.SUMMARY_NOT_FOUND);
  }
}