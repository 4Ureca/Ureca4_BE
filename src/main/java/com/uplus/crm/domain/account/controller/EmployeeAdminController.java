package com.uplus.crm.domain.account.controller;

import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;
import com.uplus.crm.domain.account.service.EmployeeAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeAdminController {

  private final EmployeeAdminService employeeAdminService;

  /** 1. 직원 계정 생성 */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmployeeCreateResponseDto createEmployee(
      @RequestBody EmployeeCreateRequestDto request
  ) {
    return employeeAdminService.createEmployee(request);
  }


  /** 3. 직원 계정 활성화 / 비활성화 */
  @PatchMapping("/{empId}/status")
  public EmployeeStatusUpdateResponseDto updateEmployeeStatus(
      @PathVariable Integer empId,
      @RequestBody EmployeeStatusUpdateRequestDto request
  ) {
    return employeeAdminService.updateEmployeeStatus(empId, request);
  }
}
