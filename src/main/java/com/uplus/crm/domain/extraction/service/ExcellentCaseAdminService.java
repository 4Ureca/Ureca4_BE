package com.uplus.crm.domain.extraction.service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@Transactional(readOnly = true)
public class ExcellentCaseAdminService {

    private final ConsultationEvaluationRepository evaluationRepository;
    private final WeeklyExcellentCaseRepository weeklyRepository;
    
    // 허용된 정렬 필드 (화이트리스트 검증)
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "score");

    /** * 1. 후보군 리스트 조회 
     * 연도(year)와 주차(week) 필터링 조건이 추가되었습니다. 
     */
    public Page<EvaluationListResponse> getCandidatePage(ExcellentCaseSearchRequest request, int page, int size) {
        // [1] 상태값 처리
        String status = ("ALL".equalsIgnoreCase(request.status()) || "string".equalsIgnoreCase(request.status())) 
                        ? null : request.status();
        
        // [2] 정렬 처리
        String sortBy = (request.sortBy() == null || request.sortBy().isBlank() || "string".equalsIgnoreCase(request.sortBy())) 
                        ? "createdAt" : request.sortBy();
        Sort.Direction direction = "asc".equalsIgnoreCase(request.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // [3] 레포지토리 호출: 날짜 계산 없이 year, week 그대로 전달! 🥊
        return evaluationRepository.findCandidatePage(
                status, 
                request.year(), 
                request.week(), 
                PageRequest.of(page, size, Sort.by(direction, sortBy))
        );
    }

    /** 2. 상세 정보 조회 */
    public EvaluationDetailResponse getDetail(Long consultId) {
        return evaluationRepository.findDetailByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }

    /** 3. 우수 사례 최종 선정 (Register) */
    @Transactional
    public boolean registerExcellentCase(Long consultId, ExcellentCaseRegisterRequest request) {
        // 1. 엔티티 조회
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
        
        if (!evaluation.isCandidate()) {
            throw new BusinessException(ErrorCode.NOT_A_CANDIDATE);
        }

        // 2. 멱등성 체크 (이미 선정된 건인지 확인)
        if (evaluation.getSelectionStatus() == SelectionStatus.SELECTED) {
            if (weeklyRepository.existsByConsultId(consultId)) {
                return false; 
            }
        }

        // 3. 상태 변경 및 주간 우수사례 테이블 저장
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

    /** 4. 우수 사례 후보 제외 또는 선정 취소 (Reject) */
    @Transactional
    public boolean rejectExcellentCase(Long consultId) {
        ConsultationEvaluation evaluation = evaluationRepository.findByConsultId(consultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));

        if (evaluation.getSelectionStatus() == SelectionStatus.REJECTED) {
            return false;
        }

        // 이미 선정된 건을 제외할 경우 주간 우수사례 테이블에서도 삭제
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