package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryListResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService {

  private final SummaryRepository summaryRepository;
  private final SummaryConsultationResultRepository consultationResultRepository;

  public Page<ConsultationSummaryListResponse> getList(Pageable pageable) {
    return summaryRepository.findAll(pageable)
        .map(ConsultationSummaryListResponse::from);
  }

  public ConsultationSummaryDetailResponse getDetail(Long id) {
    if (!consultationResultRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);
    }

    ConsultationSummary entity =
        summaryRepository.findByConsultId(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));

    return ConsultationSummaryDetailResponse.from(entity);
  }
}