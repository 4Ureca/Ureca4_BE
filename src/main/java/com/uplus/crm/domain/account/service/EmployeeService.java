package com.uplus.crm.domain.account.service;

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

import jakarta.persistence.EntityNotFoundException;
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
     * 2. 직원 상세 조회 
     */
    public EmployeeDetailResponseDto getEmployeeDetail(Integer empId) {
        Employee employee = employeeRepository.findByIdWithDetails(empId)
                .orElseThrow(() -> new EntityNotFoundException("해당 직원을 찾을 수 없습니다. ID: " + empId));

        return convertToDetailDto(employee); 
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

    private Boolean convertStatus(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) return true;
        if ("INACTIVE".equalsIgnoreCase(status)) return false;
        return null;
    }
}