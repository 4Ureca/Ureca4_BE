package com.uplus.crm.domain.account.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService{

  private final EmployeeRepository employeeRepository;
  private final EmployeeDetailRepository employeeDetailRepository;
  private final EmpPermissionRepository empPermissionRepository;
  private final DepartmentRepository departmentRepository;
  private final JobRoleRepository jobRoleRepository;
  private final PermissionRepository permissionRepository;

  /** 1. 직원 계정 생성 */
  @Override
  public EmployeeCreateResponseDto createEmployee(EmployeeCreateRequestDto request) {

    // 1) 직원 기본 계정 생성
    Employee employee = Employee.builder()
        .loginId(request.getLoginId())
        .password(request.getPassword()) // TODO: 암호화 필요
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

    Department department = departmentRepository.findById(request.getDeptId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다."));

    JobRole jobRole = jobRoleRepository.findById(request.getJobRoleId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직무입니다."));

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
        throw new IllegalArgumentException("유효하지 않은 permissionId가 포함되어 있습니다.");
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
  public EmployeePermissionUpdateResponseDto updateEmployeePermissions(Long empId,
      EmployeePermissionUpdateRequestDto request) {

    Employee employee = employeeRepository.findById(empId)
        .orElseThrow(() -> new IllegalArgumentException("해당 직원이 존재하지 않습니다."));

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
      throw new IllegalArgumentException("유효하지 않은 권한 ID가 포함되어 있습니다.");
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
  public EmployeeStatusUpdateResponseDto updateEmployeeStatus(Long empId,
      EmployeeStatusUpdateRequestDto request) {
        Employee employee = employeeRepository.findById(empId)
            .orElseThrow(() -> new IllegalArgumentException("해당 직원이 존재하지 않습니다."));

        employee.changeActiveStatus(request.getIsActive());

        return new EmployeeStatusUpdateResponseDto(
            employee.getEmpId(),
            employee.getName(),
            employee.getIsActive(),
            employee.getIsActive() ? "계정이 활성화되었습니다." : "계정이 비활성화되었습니다."
        );
    }
}
