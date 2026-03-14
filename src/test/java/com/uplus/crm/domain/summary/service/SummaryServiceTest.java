package com.uplus.crm.domain.summary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

  @InjectMocks
  private SummaryService summaryService;

  @Mock
  private SummaryRepository summaryRepository;

  @Mock
  private SummaryConsultationResultRepository consultationResultRepository;

  @Test
  @DisplayName("getDetail - 상담 결과가 없으면 CONSULTATION_RESULT_NOT_FOUND 예외를 던진다")
  void getDetail_whenConsultationResultMissing_throwsException() {
    Long consultId = 100L;
    given(consultationResultRepository.existsById(consultId)).willReturn(false);

    assertThatThrownBy(() -> summaryService.getDetail(consultId))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.CONSULTATION_RESULT_NOT_FOUND));

    then(summaryRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("getDetail - 요약 문서가 없으면 SUMMARY_NOT_FOUND 예외를 던진다")
  void getDetail_whenSummaryMissing_throwsException() {
    Long consultId = 101L;
    given(consultationResultRepository.existsById(consultId)).willReturn(true);
    given(summaryRepository.findByConsultId(consultId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> summaryService.getDetail(consultId))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SUMMARY_NOT_FOUND));
  }

  @Test
  @DisplayName("getDetail - 상담 결과와 요약이 있으면 상세 응답을 반환한다")
  void getDetail_whenExists_returnsDetailResponse() {
    Long consultId = 102L;
    LocalDateTime consultedAtUtc = LocalDateTime.of(2025, 1, 2, 1, 30);
    LocalDateTime createdAtUtc = LocalDateTime.of(2025, 1, 2, 2, 0);

    ConsultationSummary summary = ConsultationSummary.builder()
        .id("summary-1")
        .consultId(consultId)
        .consultedAt(consultedAtUtc)
        .createdAt(createdAtUtc)
        .channel("CALL")
        .durationSec(320)
        .build();

    given(consultationResultRepository.existsById(consultId)).willReturn(true);
    given(summaryRepository.findByConsultId(consultId)).willReturn(Optional.of(summary));

    ConsultationSummaryDetailResponse response = summaryService.getDetail(consultId);

    assertThat(response.getId()).isEqualTo("summary-1");
    assertThat(response.getConsultId()).isEqualTo(consultId);
    assertThat(response.getChannel()).isEqualTo("CALL");
    assertThat(response.getDurationSec()).isEqualTo(320);
    assertThat(response.getConsultedAt()).isEqualTo(LocalDateTime.of(2025, 1, 2, 10, 30));
    assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 2, 11, 0));
  }
}
