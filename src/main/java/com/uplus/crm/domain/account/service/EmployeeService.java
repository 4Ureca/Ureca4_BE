package com.uplus.crm.domain.account.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeDetailResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto.EmployeeDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * 1. 직원 목록 조회
     */
    public EmployeeListResponseDto getEmployeeList(EmployeeSearchRequestDto requestDto) {
        // [로직 1] 상태값 변환 및 검증 (활성화, 비활성화)
        Boolean isActive = validateAndConvertStatus(requestDto.getStatus());
        
        // [로직 2] ID 유효성 검증 (-1 등을 잡아냄)
        validateSearchIds(requestDto.getDept_id(), requestDto.getJob_role_id());

        // [로직 3] 페이징 안전장치 
        int page = (requestDto.getPage() == null || requestDto.getPage() < 0) ? 0 : requestDto.getPage();
        int size = (requestDto.getSize() == null || requestDto.getSize() <= 0) ? 20 : requestDto.getSize();

        // [로직 4] 레포지토리 호출
        Page<Employee> employeePage = employeeRepository.searchEmployees(
                requestDto.getDept_id(),
                requestDto.getJob_role_id(),
                isActive,
                requestDto.getKeyword(),
                PageRequest.of(page, size) 
        );

        return EmployeeListResponseDto.builder()
                .content(employeePage.getContent().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .page(employeePage.getNumber())
                .size(employeePage.getSize())
                .build();
    }

    /**
     * 2. 직원 상세 조회 
     */
    public EmployeeDetailResponseDto getEmployeeDetail(Integer empId) {
        Employee employee = employeeRepository.findByIdWithDetails(empId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.EMPLOYEE_NOT_FOUND,
                    "직원 정보를 찾을 수 없습니다. ID: " + empId
                ));

        return convertToDetailDto(employee);
    }

    // --- 내부 검증 및 변환 헬퍼 메서드 ---

    private Boolean validateAndConvertStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status) || "전체".equals(status)) {
            return null;
        }

        switch (status) { 
            case "활성화":
                return true;
            case "비활성화":
                return false;
            default:
                throw new BusinessException(ErrorCode.INVALID_INPUT, 
                    "상태값은 '활성화', '비활성화'여야 합니다.");
        }
    }

    private void validateSearchIds(Integer deptId, Integer jobRoleId) {
        if (deptId != null && deptId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "부서 ID가 유효하지 않습니다.");
        }
        if (jobRoleId != null && jobRoleId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "직무 ID가 유효하지 않습니다.");
        }
    }

    private EmployeeDto convertToDto(Employee e) {
        EmployeeDetail detail = e.getEmployeeDetail();
        return EmployeeDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .isActive(e.getIsActive())
                .deptName(detail != null && detail.getDepartment() != null ? detail.getDepartment().getDeptName() : "미지정")
                .roleName(detail != null && detail.getJobRole() != null ? detail.getJobRole().getRoleName() : "미지정")
                .joinedAt(detail != null && detail.getJoinedAt() != null ? detail.getJoinedAt().toString() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private EmployeeDetailResponseDto convertToDetailDto(Employee e) {
        EmployeeDetail detail = e.getEmployeeDetail();
        return EmployeeDetailResponseDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .birth(e.getBirth() != null ? e.getBirth().toString() : null)
                .gender(e.getGender())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .deptId(detail != null && detail.getDepartment() != null ? detail.getDepartment().getDeptId() : null)
                .deptName(detail != null && detail.getDepartment() != null ? detail.getDepartment().getDeptName() : null)
                .jobRoleId(detail != null && detail.getJobRole() != null ? detail.getJobRole().getJobRoleId() : null)
                .roleName(detail != null && detail.getJobRole() != null ? detail.getJobRole().getRoleName() : null)
                .joinedAt(detail != null && detail.getJoinedAt() != null ? detail.getJoinedAt().toString() : null)
                .build();
    }
}