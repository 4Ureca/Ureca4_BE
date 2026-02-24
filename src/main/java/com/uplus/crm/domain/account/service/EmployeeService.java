package com.uplus.crm.domain.account.service;

import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
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

    public EmployeeListResponseDto getEmployeeList(EmployeeSearchRequestDto requestDto) {
        // 1. 활성화 상태 필터 변환
        Boolean isActive = null;
        if ("ACTIVE".equalsIgnoreCase(requestDto.getStatus())) isActive = true;
        else if ("INACTIVE".equalsIgnoreCase(requestDto.getStatus())) isActive = false;

        // 2. 검색 및 페이징 수행
        Page<Employee> employeePage = employeeRepository.searchEmployees(
                requestDto.getDeptId(),
                requestDto.getJobRoleId(),
                isActive,
                requestDto.getKeyword(),
                PageRequest.of(requestDto.getPage(), requestDto.getSize())
        );

        // 3. 결과 변환 및 반환
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

    private EmployeeDto convertToDto(Employee e) {
        EmployeeDetail detail = e.getEmployeeDetail();
        
        return EmployeeDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .isActive(e.getIsActive())
                // detail, department, jobRole이 null이 아님을 가정 (inner join 조건)
                .deptName(detail != null ? detail.getDepartment().getDeptName() : "미지정")
                .roleName(detail != null ? detail.getJobRole().getRoleName() : "미지정")
                .joinedAt(detail != null && detail.getJoinedAt() != null ? detail.getJoinedAt().toString() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }
}