package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.EmployeeSearchRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeListResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeDetailResponseDto;
import com.uplus.crm.domain.account.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Employee Management", description = "관리자용 직원 계정 관리 API")
@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "직원 계정 정보 목록 조회", description = "필터링 및 키워드 검색을 포함한 직원 목록을 페이징하여 조회합니다.")
    @GetMapping
    public ResponseEntity<EmployeeListResponseDto> getEmployees(
            @ParameterObject EmployeeSearchRequestDto requestDto) {
        return ResponseEntity.ok(employeeService.getEmployeeList(requestDto));
    }

    @Operation(summary = "직원 계정 정보 상세 조회", description = "특정 직원의 모든 상세 정보와 권한 목록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDetailResponseDto> getEmployeeDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(employeeService.getEmployeeDetail(id));
    }
}