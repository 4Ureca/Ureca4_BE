package com.uplus.crm.domain.manual.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /** 3. 조회 (Read): 페이징 + 카테고리/활성여부 필터링 추가 ⭐ */
    public Page<ManualResponse> getHistory(String categoryCode, Boolean isActive, Pageable pageable) {
        
        // 1. 카테고리 코드 검증
        if (categoryCode != null && !categoryCode.isBlank()) {
            if (!policyRepository.existsById(categoryCode)) {
                throw new BusinessException(ErrorCode.CATEGORY_POLICY_NOT_FOUND);
            }
        }

        Page<Manual> manuals;

        // 2. 필터 조합에 따른 분기 처리 🥊
        if (categoryCode == null || categoryCode.isBlank()) {
            // [케이스 A] 전체 카테고리 대상
            if (isActive == null) {
                manuals = manualRepository.findAll(pageable); // 필터 없음
            } else {
                manuals = manualRepository.findAllByIsActive(isActive, pageable); // 활성여부만
            }
        } else {
            // [케이스 B] 특정 카테고리 대상
            if (isActive == null) {
                manuals = manualRepository.findAllByCategoryPolicy_CategoryCode(categoryCode, pageable); // 카테고리만
            } else {
                manuals = manualRepository.findAllByCategoryPolicy_CategoryCodeAndIsActive(categoryCode, isActive, pageable); // 카테고리 + 활성여부
            }
        }

        // 3. Page<Manual>을 Page<ManualResponse>로 변환하여 반환 🥊
        return manuals.map(ManualResponse::from);
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