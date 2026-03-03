package com.uplus.crm.domain.account.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.repository.mysql.DepartmentRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeDetailRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.JobRoleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAdminServiceImpl implements EmployeeAdminService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDetailRepository employeeDetailRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRoleRepository jobRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /** 직원 계정 생성 */
    @Override
    public EmployeeCreateResponseDto createEmployee(EmployeeCreateRequestDto request) {
        if (employeeRepository.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Employee employee = Employee.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .birth(request.getBirth())
                .gender(request.getGender())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        Department department = departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
        JobRole jobRole = jobRoleRepository.findById(request.getJobRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_ROLE_NOT_FOUND));

        EmployeeDetail detail = EmployeeDetail.builder()
                .employee(savedEmployee)
                .department(department)
                .jobRole(jobRole)
                .joinedAt(request.getJoinedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        employeeDetailRepository.save(detail);

        return new EmployeeCreateResponseDto(
                savedEmployee.getEmpId(),
                savedEmployee.getLoginId(),
                savedEmployee.getName(),
                detail.getDepartment().getDeptName(),
                detail.getJobRole().getRoleName(),
                savedEmployee.getCreatedAt()
        );
    }

    /** 직원 계정 활성화 / 비활성화 */
    @Override
    public EmployeeStatusUpdateResponseDto updateEmployeeStatus(Integer empId,
            EmployeeStatusUpdateRequestDto request) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employee.changeActiveStatus(request.getIsActive());

        return new EmployeeStatusUpdateResponseDto(
                employee.getEmpId(),
                employee.getName(),
                employee.getIsActive(),
                employee.getIsActive() ? "계정이 활성화되었습니다." : "계정이 비활성화되었습니다."
        );
    }

    /** 직원 계정 정보 수정 (관리자) */
    @Override
    public AdminEmployeeUpdateResponseDto updateEmployee(Integer empId, AdminEmployeeUpdateRequestDto req) {
        if (req == null || req.getName() == null || req.getName().isBlank()
                || req.getDeptId() == null || req.getJobRoleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (employeeRepository.existsByEmailAndEmpIdNot(req.getEmail(), empId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Department dept = departmentRepository.findById(req.getDeptId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
        JobRole role = jobRoleRepository.findById(req.getJobRoleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));

        LocalDate birth = parseLocalDateOrNull(req.getBirth());
        LocalDate joinedAt = parseLocalDateOrNull(req.getJoinedAt());

        employee.updateAccountInfo(req.getName(), req.getEmail(), req.getPhone(), birth, req.getGender());

        EmployeeDetail detail = employeeDetailRepository.findById(empId)
                .orElseGet(() -> EmployeeDetail.builder()
                        .empId(empId)
                        .employee(employee)
                        .department(dept)
                        .jobRole(role)
                        .joinedAt(joinedAt)
                        .build()
                );

        detail.updateDetail(dept, role, joinedAt);
        employeeDetailRepository.save(detail);

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
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
