package com.uplus.crm.domain.account.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeePermissionUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeePermissionUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeePermissionUpdateResponseDto.PermissionDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;
import com.uplus.crm.domain.account.entity.Department;
import com.uplus.crm.domain.account.entity.EmpPermission;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.entity.JobRole;
import com.uplus.crm.domain.account.entity.Permission;
import com.uplus.crm.domain.account.repository.mysql.DepartmentRepository;
import com.uplus.crm.domain.account.repository.mysql.EmpPermissionRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeDetailRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.JobRoleRepository;
import com.uplus.crm.domain.account.repository.mysql.PermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeAdminServiceImpl implements EmployeeAdminService{

  private final EmployeeRepository employeeRepository;
  private final EmployeeDetailRepository employeeDetailRepository;
  private final EmpPermissionRepository empPermissionRepository;
  private final DepartmentRepository departmentRepository;
  private final JobRoleRepository jobRoleRepository;
  private final PermissionRepository permissionRepository;
  private final PasswordEncoder passwordEncoder;

  /** 1. 직원 계정 생성 */
  @Override
  public EmployeeCreateResponseDto createEmployee(EmployeeCreateRequestDto request) {

    // 중복 체크 (409)
    if (employeeRepository.existsByLoginId(request.getLoginId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    if (employeeRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 1) 직원 기본 계정 생성
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


    // 2) 직원 상세 정보 생성 (1:1 매핑, @MapsId 구조)

    // 부서 조회 (404)
    Department department = departmentRepository.findById(request.getDeptId())
        .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

  // 직무 조회 (404)
    JobRole jobRole = jobRoleRepository.findById(request.getJobRoleId())
        .orElseThrow(() -> new BusinessException(ErrorCode.JOB_ROLE_NOT_FOUND));

    EmployeeDetail detail = EmployeeDetail.builder()
        .employee(savedEmployee)
        .department(department) // id만 세팅
        .jobRole(jobRole)
        .joinedAt(request.getJoinedAt())
        .updatedAt(LocalDateTime.now())
        .build();

    employeeDetailRepository.save(detail);


    // 3) 개별 권한 저장
    if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {

      List<Permission> permissions =
          permissionRepository.findAllById(request.getPermissionIds());

      if (permissions.size() != request.getPermissionIds().size()) {
        throw new BusinessException(ErrorCode.INVALID_PERMISSION_IDS);
      }

      List<EmpPermission> empPermissions = permissions.stream()
          .map(permission -> EmpPermission.builder()
              .employee(savedEmployee)
              .permission(permission)
              .assignedAt(LocalDateTime.now())
              .isDeleted(false)
              .build())
          .toList();

      empPermissionRepository.saveAll(empPermissions);
    }


    return new EmployeeCreateResponseDto(
        savedEmployee.getEmpId(),
        savedEmployee.getLoginId(),
        savedEmployee.getName(),
        detail.getDepartment().getDeptName(),
        detail.getJobRole().getRoleName(),
        savedEmployee.getCreatedAt()
    );
  }


  /**
   * 2. 직원 개별 권한 편집 (전체 교체)
   */
  @Override
  public EmployeePermissionUpdateResponseDto updateEmployeePermissions(Integer empId,
      EmployeePermissionUpdateRequestDto request) {

    Employee employee = employeeRepository.findById(empId)
        .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

    if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {

      empPermissionRepository.softDeleteByEmployeeId(empId);

      return new EmployeePermissionUpdateResponseDto(
          employee.getEmpId(),
          employee.getName(),
          List.of()
      );
    }

    // 기존 권한 삭제
//    empPermissionRepository.deleteByEmployee_EmpId(empId);
    // soft_deleted
    empPermissionRepository.softDeleteByEmployeeId(empId);

    // Permission 엔티티 조회
    List<Permission> permissions =
        permissionRepository.findAllById(request.getPermissionIds());

    // 유효성 검증
    if (permissions.size() != request.getPermissionIds().size()) {
      throw new BusinessException(ErrorCode.INVALID_PERMISSION_IDS);
    }

    // EmpPermission 엔티티 생성
    List<EmpPermission> newPermissions = permissions.stream()
        .map(permission -> EmpPermission.builder()
            .employee(employee)
            .permission(permission)
            .assignedAt(LocalDateTime.now())
            .isDeleted(false)
            .build())
        .toList();

    empPermissionRepository.saveAll(newPermissions);

    // Response DTO 변환
    List<PermissionDto> permissionDtos = newPermissions.stream()
        .map(empPerm -> new PermissionDto(
            empPerm.getPermission().getPermId(),
            empPerm.getPermission().getPermCode(),
            empPerm.getPermission().getPermDesc(),
            empPerm.getAssignedAt()
        ))
        .toList();

    return new EmployeePermissionUpdateResponseDto(
        employee.getEmpId(),
        employee.getName(),
        permissionDtos
    );
  }


  /** 3. 직원 계정 활성화 / 비활성화 */
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
}
