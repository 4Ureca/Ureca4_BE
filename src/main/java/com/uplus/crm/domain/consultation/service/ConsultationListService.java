package com.uplus.crm.domain.consultation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.consultation.dto.response.ConsultationListResponseDto;
import com.uplus.crm.domain.consultation.repository.ConsultationListQueryRepository;

import lombok.RequiredArgsConstructor;

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
        // 1. 한글 파라미터를 DB용 영문 값으로 변환
        String dbChannel = convertChannelToDbValue(channel);
        String dbSummaryStatus = convertSummaryStatusToDbValue(summaryStatus);
        String dbResultStatus = convertResultStatusToDbValue(resultStatus);

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int offset = safePage * safeSize;

        // 2. 변환된 값(dbChannel 등)을 리포지토리에 전달
        long totalElements = consultationListQueryRepository.countConsultationList(
                keyword,
                dbChannel,
                categoryCode,
                dbSummaryStatus,
                dbResultStatus
        );

        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);

        return ConsultationListResponseDto.builder()
                .content(consultationListQueryRepository.findConsultationList(
                        keyword,
                        dbChannel,
                        categoryCode,
                        dbSummaryStatus,
                        dbResultStatus,
                        offset,
                        safeSize
                ))
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 채널 한글명을 DB 값으로 변환
     */
    private String convertChannelToDbValue(String channel) {
        if (channel == null || channel.isBlank()) return channel;
        return switch (channel) {
            case "전화" -> "CALL";
            case "채팅" -> "CHATTING";
            default -> channel; 
        };
    }

    /**
     * 요약 상태 한글명을 DB 값으로 변환 (소문자 completed/requested/failed)
     */
    private String convertSummaryStatusToDbValue(String status) {
        if (status == null || status.isBlank()) return status;
        return switch (status) {
            case "요약완료" -> "completed";
            case "요청중" -> "requested";
            case "실패" -> "failed";
            default -> status;
        };
    }

    /**
     * 처리 상태 한글명을 DB 값으로 변환 (대문자 PROCESSING/COMPLETED 등)
     */
    private String convertResultStatusToDbValue(String status) {
        if (status == null || status.isBlank()) return status;
        return switch (status) {
            case "처리중" -> "PROCESSING";
            case "완료" -> "COMPLETED";
            case "미완료" -> "FAILED";
            case "요청중" -> "REQUESTED";
            default -> status;
        };
    }
}