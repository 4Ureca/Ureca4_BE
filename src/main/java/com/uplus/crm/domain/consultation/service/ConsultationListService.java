package com.uplus.crm.domain.consultation.service;

import com.uplus.crm.domain.consultation.dto.response.ConsultationListResponseDto;
import com.uplus.crm.domain.consultation.repository.ConsultationListQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationListService {

    private final ConsultationListQueryRepository consultationListQueryRepository;

    public ConsultationListResponseDto getConsultationList(
            String keyword,
            String channel,
            String categoryCode,
            String summaryStatus,
            String resultStatus,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset = safePage * safeSize;

        long totalElements = consultationListQueryRepository.countConsultationList(
                keyword,
                channel,
                categoryCode,
                summaryStatus,
                resultStatus
        );

        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        return ConsultationListResponseDto.builder()
                .content(consultationListQueryRepository.findConsultationList(
                        keyword,
                        channel,
                        categoryCode,
                        summaryStatus,
                        resultStatus,
                        offset,
                        safeSize
                ))
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
