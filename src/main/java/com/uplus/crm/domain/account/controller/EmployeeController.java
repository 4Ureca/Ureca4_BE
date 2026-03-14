package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.AdminEmployeeUpdateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.AdminEmployeeUpdateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeDetailResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;
import com.uplus.crm.domain.account.service.EmployeeAdminService;
import com.uplus.crm.domain.account.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Employee Management", description = "관리자용 직원 계정 관리 API")
@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeAdminService employeeAdminService;

    @Operation(summary = "직원 계정 생성", description = "신규 직원의 계정을 생성한다.")
    @PostMapping
    public ResponseEntity<EmployeeCreateResponseDto> createEmployee(
            @RequestBody EmployeeCreateRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeAdminService.createEmployee(request));
    }

    @Operation(summary = "직원 계정 정보 수정", description = "직원의 이름·이메일·부서·역할 등 기본 정보를 수정한다.")
    @PutMapping("/{id}")
    public ResponseEntity<AdminEmployeeUpdateResponseDto> updateEmployee(
            @PathVariable("id") Integer id,
            @RequestBody AdminEmployeeUpdateRequestDto request
    ) {
        return ResponseEntity.ok(employeeAdminService.updateEmployee(id, request));
    }

    // [수정된 부분] @PathVariable 뒤에 ("empId")를 명시했습니다.
    @Operation(summary = "직원 계정 활성화/비활성화", description = "직원 계정을 활성화/비활성화한다.")
    @PatchMapping("/{empId}/status")
    public ResponseEntity<EmployeeStatusUpdateResponseDto> updateEmployeeStatus(
            @PathVariable("empId") Integer empId, 
            @RequestBody EmployeeStatusUpdateRequestDto request
    ) {
        return ResponseEntity.ok(employeeAdminService.updateEmployeeStatus(empId, request));
    }

    @Operation(summary = "직원 계정 정보 목록 조회", description = "필터링 및 키워드 검색을 포함한 직원 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<EmployeeListResponseDto> getEmployees(
            @ParameterObject EmployeeSearchRequestDto requestDto
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeList(requestDto));
    }

    @Operation(summary = "직원 계정 정보 상세 조회", description = "특정 직원의 모든 상세 정보와 권한 목록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailResponseDto> getEmployeeDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeDetail(id));
    }
}