package com.uplus.crm.domain.account.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.account.dto.response.EmailCheckResponseDto;
import com.uplus.crm.domain.account.dto.response.MyInfoResponseDto;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.entity.EmployeeDetail;
import com.uplus.crm.domain.account.repository.mysql.DeptPermissionRepository;
import com.uplus.crm.domain.account.repository.mysql.EmpPermissionRepository;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final DeptPermissionRepository deptPermissionRepository;
    private final EmpPermissionRepository empPermissionRepository;

    /**
     * 1. 구글 이메일 중복 확인
     */
    public EmailCheckResponseDto checkEmailAvailability(String email) {
        boolean isDuplicate = employeeRepository.existsByEmail(email);
        
        return EmailCheckResponseDto.builder()
                .available(!isDuplicate)
                .email(email)
                .build();
    }

    /**
     * 2. 로그인한 계정 정보 조회 (내 정보 조회)
     */
    public MyInfoResponseDto getMyInfo(Integer empId) {
        // 직원 및 연관 정보(상세, 부서, 역할) 조회
        // Repository에 선언된 findByIdWithDetails(empId) 메서드를 호출합니다.
        Employee employee = employeeRepository.findByIdWithDetails(empId)
                .orElseThrow(() -> new EntityNotFoundException("로그인된 사용자 정보를 찾을 수 없습니다. ID: " + empId));

        EmployeeDetail detail = employee.getEmployeeDetail();
        
        // 권한 코드 합산 (Set을 사용하여 중복 자동 제거)
        Set<String> allPermissions = new HashSet<>();
        
        // A. 부서 권한 추가
        if (detail != null && detail.getDepartment() != null) {
            allPermissions.addAll(deptPermissionRepository.findPermCodesByDeptId(detail.getDepartment().getDeptId()));
        }
        
        // B. 개별 권한 추가
        allPermissions.addAll(empPermissionRepository.findPermCodesByEmpId(empId));

        return convertToMyInfoDto(employee, allPermissions);
    }

    // --- Private Helper Methods ---

    private MyInfoResponseDto convertToMyInfoDto(Employee e, Set<String> perms) {
        EmployeeDetail d = e.getEmployeeDetail();
        
        return MyInfoResponseDto.builder()
                .empId(e.getEmpId())
                .loginId(e.getLoginId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .birth(e.getBirth() != null ? e.getBirth().toString() : null)
                .gender(e.getGender())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .deptId(d != null ? d.getDepartment().getDeptId() : null)
                .deptName(d != null ? d.getDepartment().getDeptName() : null)
                .jobRoleId(d != null ? d.getJobRole().getJobRoleId() : null)
                .roleName(d != null ? d.getJobRole().getRoleName() : null)
                .joinedAt(d != null && d.getJoinedAt() != null ? d.getJoinedAt().toString() : null)
                .permissions(new ArrayList<>(perms))
                .build();
    }
}