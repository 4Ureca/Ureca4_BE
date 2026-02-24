package com.uplus.crm.domain.account.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto.EmployeeDto;
import com.uplus.crm.domain.account.dto.response.EmployeeDetailResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeDetailResponseDto.PermissionDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.account.repository.mysql.DeptPermissionRepository;
import com.uplus.crm.domain.account.repository.mysql.EmpPermissionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DeptPermissionRepository deptPermissionRepository;
    private final EmpPermissionRepository empPermissionRepository;

    /**
     * 1. 직원 목록 조회 (기존 기능)
     */
    public EmployeeListResponseDto getEmployeeList(EmployeeSearchRequestDto requestDto) {
        Boolean isActive = convertStatus(requestDto.getStatus());

        Page<Employee> employeePage = employeeRepository.searchEmployees(
                requestDto.getDeptId(),
                requestDto.getJobRoleId(),
                isActive,
                requestDto.getKeyword(),
                PageRequest.of(requestDto.getPage(), requestDto.getSize())
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
     * 2. 직원 상세 조회 (신규 추가)
     */
    public EmployeeDetailResponseDto getEmployeeDetail(Integer empId) {
        // 직원 & 상세 정보 & 부서/역할 한 번에 조회 (Repository에 findByIdWithDetails 필요)
        Employee employee = employeeRepository.findByIdWithDetails(empId)
                .orElseThrow(() -> new EntityNotFoundException("해당 직원을 찾을 수 없습니다. ID: " + empId));

        EmployeeDetail detail = employee.getEmployeeDetail();
        Integer deptId = (detail != null && detail.getDepartment() != null) ? detail.getDepartment().getDeptId() : null;

        // 부서 권한 조회
        List<PermissionDto> deptPerms = (deptId != null) ? 
            deptPermissionRepository.findByDeptIdWithPermission(deptId).stream()
                .map(dp -> PermissionDto.builder()
                        .permId(dp.getPermission().getPermId())
                        .permCode(dp.getPermission().getPermCode())
                        .permDesc(dp.getPermission().getPermDesc())
                        .build())
                .collect(Collectors.toList()) : List.of();

        // 개별 권한 조회 (isDeleted = false 인 것만)
        List<PermissionDto> empPerms = empPermissionRepository.findByEmpIdWithPermission(empId, false).stream()
                .map(ep -> PermissionDto.builder()
                        .permId(ep.getPermission().getPermId())
                        .permCode(ep.getPermission().getPermCode())
                        .permDesc(ep.getPermission().getPermDesc())
                        .build())
                .collect(Collectors.toList());

        return convertToDetailDto(employee, deptPerms, empPerms);
    }

    // --- private Helper Methods ---

    private EmployeeDto convertToDto(Employee e) {
        EmployeeDetail detail = e.getEmployeeDetail();
        return EmployeeDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .isActive(e.getIsActive())
                .deptName(detail != null ? detail.getDepartment().getDeptName() : "미지정")
                .roleName(detail != null ? detail.getJobRole().getRoleName() : "미지정")
                .joinedAt(detail != null && detail.getJoinedAt() != null ? detail.getJoinedAt().toString() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private EmployeeDetailResponseDto convertToDetailDto(Employee e, List<PermissionDto> deptPerms, List<PermissionDto> empPerms) {
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
                .deptId(detail != null ? detail.getDepartment().getDeptId() : null)
                .deptName(detail != null ? detail.getDepartment().getDeptName() : null)
                .jobRoleId(detail != null ? detail.getJobRole().getJobRoleId() : null)
                .roleName(detail != null ? detail.getJobRole().getRoleName() : null)
                .joinedAt(detail != null && detail.getJoinedAt() != null ? detail.getJoinedAt().toString() : null)
                .deptPermissions(deptPerms)
                .empPermissions(empPerms)
                .build();
    }

    private Boolean convertStatus(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) return true;
        if ("INACTIVE".equalsIgnoreCase(status)) return false;
        return null;
    }
}