package com.uplus.crm.domain.extraction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.entity.SelectionStatus;
import com.uplus.crm.domain.extraction.repository.ConsultationEvaluationRepository;

import java.util.Set;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcellentCaseAdminService {
    private final ConsultationEvaluationRepository evaluationRepository;
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "score");

    @Transactional(readOnly = true)
    public Page<EvaluationListResponse> getCandidatePage(ExcellentCaseSearchRequest request, int page, int size) {
        String status = request.status();
        if (status == null || status.isBlank() || "string".equalsIgnoreCase(status) || "ALL".equalsIgnoreCase(status)) {
            status = null;
        } else {
            try {
                SelectionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_SELECTION_STATUS);
            }
        }
        String sortBy = request.sortBy();
        if (sortBy == null || sortBy.isBlank() || "string".equalsIgnoreCase(sortBy)) {
            sortBy = "createdAt";
        } else if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException(ErrorCode.INVALID_SORT_FIELD);
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String dirInput = request.direction();
        
        if (dirInput != null && !dirInput.isBlank() && !"string".equalsIgnoreCase(dirInput)) {
            try {
                direction = Sort.Direction.fromString(dirInput);
            } catch (IllegalArgumentException e) {
                direction = Sort.Direction.DESC;
            }
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return evaluationRepository.findCandidatePage(status, pageable);
    }

    @Transactional(readOnly = true)
    public EvaluationDetailResponse getDetail(Long consultId) {
        return evaluationRepository.findDetailByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }
}