package com.uplus.crm.domain.extraction.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder; 

import com.uplus.crm.domain.extraction.entity.ExcellentEventStatus;
import com.uplus.crm.domain.extraction.entity.ResultEventStatus;
import com.uplus.crm.domain.extraction.repository.ExcellentEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.ResultEventStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisRetryService {

    private final ResultEventStatusRepository resultRepository;
    private final ExcellentEventStatusRepository excellentRepository;

    public void retryConsultations(List<Long> consultIds) {
        String adminId = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("[Admin Manual Retry Start] 관리자: {}, 재처리 대상 건수: {}건, ID 리스트: {}", 
                 adminId, consultIds.size(), consultIds);

        try {
            // 1. 요약 상태 테이블 재설정
            List<ResultEventStatus> resultTasks = resultRepository.findAllByConsultIdIn(consultIds);
            resultTasks.forEach(ResultEventStatus::retry);

            // 2. 채점 상태 테이블 재설정
            List<ExcellentEventStatus> excellentTasks = excellentRepository.findAllByConsultIdIn(consultIds);
            excellentTasks.forEach(ExcellentEventStatus::retry);

            log.info("[Admin Manual Retry Success] 관리자: {}, {}건의 상태가 REQUESTED로 초기화되었습니다.", 
                     adminId, consultIds.size());

        } catch (Exception e) {
            log.error("[Admin Manual Retry Failure] 재처리 시도 중 오류 발생! 사유: {}", e.getMessage(), e);
            throw e; 
        }
    }
}