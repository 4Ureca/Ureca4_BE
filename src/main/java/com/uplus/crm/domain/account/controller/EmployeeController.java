package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto;
import com.uplus.crm.domain.account.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Employee Management", description = "관리자용 직원 계정 관리 API")
@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "직원 계정 정보 목록 조회", description = "부서, 역할, 활성 상태 필터 및 키워드 검색을 포함한 직원 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<EmployeeListResponseDto> getEmployees(
            @ParameterObject EmployeeSearchRequestDto requestDto) {
        return ResponseEntity.ok(employeeService.getEmployeeList(requestDto));
    }
}