package com.uplus.crm.domain.account.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.DepartmentRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeDetailRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.JobRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDetailRepository employeeDetailRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;

    @Transactional
    public AdminEmployeeUpdateResponseDto updateEmployee(Integer empId, AdminEmployeeUpdateRequestDto req) {

        // 0) 필수 파라미터 검증 (400)
        if (req == null
                || isBlank(req.getName())
                || isBlank(req.getEmail())
                || req.getDeptId() == null
                || req.getJobRoleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        // 1) 직원 존재 확인 (404)
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 2) 이메일 중복 체크 (409)
        if (employeeRepository.existsByEmailAndEmpIdNot(req.getEmail(), empId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 3) 부서/역할 존재 확인 (400)
        Department dept = departmentRepository.findById(req.getDeptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        JobRole role = jobRoleRepository.findById(req.getJobRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        // 4) 문자열 날짜 파싱 (nullable) + 포맷 틀리면 400
        LocalDate birth = parseLocalDateOrNull(req.getBirth());
        LocalDate joinedAt = parseLocalDateOrNull(req.getJoinedAt());

        // 5) Employee 업데이트
        employee.updateAccountInfo(
                req.getName(),
                req.getEmail(),
                req.getPhone(),
                birth,
                req.getGender()
        );

        // 6) EmployeeDetail 조회/없으면 생성 후 업데이트
        EmployeeDetail detail = employeeDetailRepository.findById(empId.longValue())
                .orElseGet(() -> EmployeeDetail.builder()
                        .empId(empId)
                        .employee(employee)
                        .department(dept)
                        .jobRole(role)
                        .joinedAt(joinedAt)
                        .build()
                );

        detail.updateDetail(dept, role, joinedAt);

        // 7) 저장
        employeeDetailRepository.save(detail);

        // 8) 응답 DTO
        return AdminEmployeeUpdateResponseDto.builder()
                .empId(employee.getEmpId())
                .name(employee.getName())
                .email(employee.getEmail())
                .deptName(detail.getDepartment().getDeptName())
                .roleName(detail.getJobRole().getRoleName())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value); // "yyyy-MM-dd"
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}