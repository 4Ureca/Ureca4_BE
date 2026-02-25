package com.uplus.crm.domain.account.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uplus.crm.domain.account.dto.request.EmpPermissionRequestDto;
import com.uplus.crm.domain.account.dto.response.EmpPermissionListResponseDto;
import com.uplus.crm.domain.account.service.EmpPermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "직원 권한 관리", description = "직원의 개별 권한을 조회하고 관리하는 API")
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class EmpPermissionController {
	private final EmpPermissionService empPermissionService;
	
    @Operation(summary = "직원별 보유 권한 목록 조회", description = "직원 ID를 통해 해당 직원이 가진 모든 권한을 리스트로 반환합니다.")
    @GetMapping
    public ResponseEntity<EmpPermissionListResponseDto> getPermissions(
    		@ParameterObject EmpPermissionRequestDto requestDto) {
        
        EmpPermissionListResponseDto response = empPermissionService.getEmployeePermissions(requestDto);
        return ResponseEntity.ok(response);
    }
}