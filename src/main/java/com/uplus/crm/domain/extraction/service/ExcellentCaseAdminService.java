package com.uplus.crm.domain.extraction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.repository.ConsultationEvaluationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcellentCaseAdminService {
    private final ConsultationEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public Page<EvaluationListResponse> getCandidatePage(ExcellentCaseSearchRequest request, int page, int size) {
        
        // 1. 상태값(status) 처리: "ALL"이거나 값이 없으면 null (전체조회)
        String status = request.status();
        if (status == null || status.isBlank() || "string".equalsIgnoreCase(status) || "ALL".equalsIgnoreCase(status)) {
            status = null;
        }

        // 2. 정렬 기준(sortBy) 처리: ★ 기본값을 createdAt으로 변경 ★
        String sortBy = request.sortBy();
        if (sortBy == null || sortBy.isBlank() || "string".equalsIgnoreCase(sortBy)) {
            sortBy = "createdAt"; // 첫 진입 시 최신순
        }

        // 3. 정렬 방향(direction) 처리: 기본값 DESC
        Sort.Direction direction = Sort.Direction.DESC;
        String dirInput = request.direction();
        
        if (dirInput != null && !dirInput.isBlank() && !"string".equalsIgnoreCase(dirInput)) {
            try {
                direction = Sort.Direction.fromString(dirInput);
            } catch (IllegalArgumentException e) {
                direction = Sort.Direction.DESC;
            }
        }

        // 4. 최종 Pageable 조립 (예: sortBy가 "score"로 들어오면 점수 정렬 작동)
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return evaluationRepository.findCandidatePage(status, pageable);
    }
    @Transactional(readOnly = true)
    public EvaluationDetailResponse getDetail(Long consultId) {
        return evaluationRepository.findDetailByConsultId(consultId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담의 분석 결과를 찾을 수 없습니다. ID: " + consultId));
    }
}