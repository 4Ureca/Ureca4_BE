package com.uplus.crm.domain.extraction.service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseRegisterRequest;
import com.uplus.crm.domain.extraction.dto.request.ExcellentCaseSearchRequest;
import com.uplus.crm.domain.extraction.dto.response.EvaluationDetailResponse;
import com.uplus.crm.domain.extraction.dto.response.EvaluationListResponse;
import com.uplus.crm.domain.extraction.entity.ConsultationEvaluation;
import com.uplus.crm.domain.extraction.entity.SelectionStatus;
import com.uplus.crm.domain.extraction.entity.WeeklyExcellentCase;
import com.uplus.crm.domain.extraction.repository.ConsultationEvaluationRepository;
import com.uplus.crm.domain.extraction.repository.WeeklyExcellentCaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 읽기 전용으로 설정
public class ExcellentCaseAdminService {

    private final ConsultationEvaluationRepository evaluationRepository;
    private final WeeklyExcellentCaseRepository weeklyRepository;
    
    // 허용된 정렬 필드 (화이트리스트 검증)
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "score");

    /** 1. 후보군 리스트 조회 */
    public Page<EvaluationListResponse> getCandidatePage(ExcellentCaseSearchRequest request, int page, int size) {
        String status = ("ALL".equalsIgnoreCase(request.status()) || "string".equalsIgnoreCase(request.status())) 
                        ? null : request.status();
        if (status != null) {
            try { 
                SelectionStatus.valueOf(status.toUpperCase()); 
            } catch (IllegalArgumentException e) { 
                throw new BusinessException(ErrorCode.INVALID_INPUT, "유효하지 않은 선정 상태값입니다: " + status); 
            }
        }

        String sortBy = (request.sortBy() == null || request.sortBy().isBlank() || "string".equalsIgnoreCase(request.sortBy())) 
                        ? "createdAt" : request.sortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "정렬할 수 없는 필드명입니다: " + sortBy);
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(request.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return evaluationRepository.findCandidatePage(status, PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }

    /** 2. 상세 정보 조회 */
    public EvaluationDetailResponse getDetail(Long consultId) {
        return evaluationRepository.findDetailByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }

    /** 3. 우수 사례 최종 선정 (Register)*/
    @Transactional
    public boolean registerExcellentCase(Long consultId, ExcellentCaseRegisterRequest request) {
        // 1. 엔티티 조회
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        
        if (!evaluation.isCandidate()) {
            throw new BusinessException(ErrorCode.NOT_A_CANDIDATE);
        }

        // 2. 멱등성 체크
        if (evaluation.getSelectionStatus() == SelectionStatus.SELECTED) {
            if (weeklyRepository.existsByConsultId(consultId)) {
                return false; 
            }
        }

        // 3. 상태 변경 및 이력 저장
        LocalDateTime now = LocalDateTime.now();
        evaluation.updateSelectionStatus(SelectionStatus.SELECTED);

        WeeklyExcellentCase weeklyCase = WeeklyExcellentCase.builder()
                .consultId(evaluation.getConsultId())
                .evaluationId(evaluation.getEvaluationId())
                .yearVal(now.getYear())
                .weekVal(now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()))
                .adminReason(request.adminReason())
                .selectedAt(now)
                .updatedAt(now)
                .build();

        try {
            weeklyRepository.save(weeklyCase);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.DATA_INTEGRITY_ERROR);
        }
        
        return true;
    }

    @Transactional
    public boolean rejectExcellentCase(Long consultId) {
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getSelectionStatus() == SelectionStatus.REJECTED) {
            return false;
        }

        if (evaluation.getSelectionStatus() == SelectionStatus.SELECTED) {
            try {
                weeklyRepository.deleteByConsultId(consultId);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.DATA_INTEGRITY_ERROR, "게시판 데이터 삭제 중 오류가 발생했습니다.");
            }
        }

        evaluation.updateSelectionStatus(SelectionStatus.REJECTED);
        return true;
    }
}