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
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getSelectionStatus() == SelectionStatus.SELECTED) {
            if (weeklyRepository.existsByConsultId(consultId)) {
                return false;
            }
            // 만약 상태는 SELECTED인데 이력이 없다면(데이터 불일치), 아래 로직을 수행하여 이력을 생성함
        }

        // 시간 기준점 생성
        LocalDateTime now = LocalDateTime.now();

        // 1. 평가 엔티티 상태 변경
        evaluation.updateSelectionStatus(SelectionStatus.SELECTED);

        // 2. 주간 우수 사례 이력 생성 및 저장 
        int year = now.getYear();
        int week = now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        WeeklyExcellentCase weeklyCase = WeeklyExcellentCase.builder()
                .consultId(evaluation.getConsultId())
                .evaluationId(evaluation.getEvaluationId())
                .yearVal(year)
                .weekVal(week)
                .adminReason(request.adminReason())
                .selectedAt(now)
                .updatedAt(now) 
                .build();

        weeklyRepository.save(weeklyCase);
        return true;
    }
    
    /** 4. 우수 사례 제외 (Reject)*/
    @Transactional
    public boolean rejectExcellentCase(Long consultId) {
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getSelectionStatus() == SelectionStatus.REJECTED) {
            return false;
        }

        evaluation.updateSelectionStatus(SelectionStatus.REJECTED);
        return true;
    }
}