package com.uplus.crm.domain.manual.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.manual.dto.request.ManualRequest;
import com.uplus.crm.domain.manual.dto.request.ManualUpdateRequest;
import com.uplus.crm.domain.manual.dto.response.ManualResponse;
import com.uplus.crm.domain.manual.entity.Manual;
import com.uplus.crm.domain.manual.repository.ManualRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManualService {
    private final ManualRepository manualRepository;
    private final ConsultationCategoryRepository policyRepository;
    private final EmployeeRepository employeeRepository;

    /** 1. 생성 (Create) */
    @Transactional
    public void createManual(ManualRequest request, Integer empId) {
        ConsultationCategoryPolicy policy = policyRepository.findById(request.categoryCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_POLICY_NOT_FOUND));

        Employee employee = employeeRepository.findById(empId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        manualRepository.findByCategoryPolicy_CategoryCodeAndIsActiveTrue(request.categoryCode())
            .ifPresent(old -> {
                old.setIsActive(false);
                old.setUpdatedAt(LocalDateTime.now());
            });

        Manual manual = Manual.builder()
            .categoryPolicy(policy)
            .employee(employee)
            .title(request.title())
            .content(request.content())
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        manualRepository.save(manual);
    }

    /** 2. 수정 (Update) */
    @Transactional
    public void updateManual(Integer manualId, ManualUpdateRequest request) {
        Manual manual = manualRepository.findById(manualId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));
        
        manual.setTitle(request.title());
        manual.setContent(request.content());
        manual.setUpdatedAt(LocalDateTime.now());
    }

    /** 3. 조회 (Read): 카테고리 코드가 있으면 유효성 검증 후 조회 ⭐ */
    public List<ManualResponse> getHistory(String categoryCode) {
        // 1. 카테고리 코드가 파라미터로 넘어온 경우, 실제 존재하는 코드인지 먼저 검증
        if (categoryCode != null && !categoryCode.isBlank()) {
            if (!policyRepository.existsById(categoryCode)) {
                // 존재하지 않는 카테고리 코드라면 404 에러를 던짐
                throw new BusinessException(ErrorCode.CATEGORY_POLICY_NOT_FOUND);
            }
        }

        List<Manual> manuals;
        
        // 2. 필터링 조건에 따른 조회 수행
        if (categoryCode == null || categoryCode.isBlank()) {
            // 카테고리 코드가 없으면 전체 매뉴얼 최신순 조회
            manuals = manualRepository.findAllByOrderByCreatedAtDesc();
        } else {
            // 카테고리 코드가 있으면 해당 카테고리의 매뉴얼만 최신순 조회
            manuals = manualRepository.findAllByCategoryPolicy_CategoryCodeOrderByCreatedAtDesc(categoryCode);
        }

        return manuals.stream()
            .map(ManualResponse::from)
            .toList();
    }

    /** 4. 비활성화 (Deactivate) */
    @Transactional
    public void deactivateManual(Integer manualId) {
        Manual manual = manualRepository.findById(manualId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));
        manual.setIsActive(false);
        manual.setUpdatedAt(LocalDateTime.now());
    }

    /** 5. 활성화 (Activate) */
    @Transactional
    public void activateManual(Integer manualId) {
        Manual manual = manualRepository.findById(manualId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MANUAL_NOT_FOUND));

        manualRepository.findByCategoryPolicy_CategoryCodeAndIsActiveTrue(manual.getCategoryPolicy().getCategoryCode())
            .ifPresent(old -> {
                old.setIsActive(false);
                old.setUpdatedAt(LocalDateTime.now());
            });

        manual.setIsActive(true);
        manual.setUpdatedAt(LocalDateTime.now());
    }
}