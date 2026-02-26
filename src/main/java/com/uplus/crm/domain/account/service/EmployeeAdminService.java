package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.request.EmployeeCreateRequestDto;
import com.uplus.crm.domain.account.dto.request.EmployeeStatusUpdateRequestDto;
import com.uplus.crm.domain.account.dto.response.EmployeeCreateResponseDto;
import com.uplus.crm.domain.account.dto.response.EmployeeStatusUpdateResponseDto;

public interface EmployeeAdminService {
  EmployeeCreateResponseDto createEmployee(EmployeeCreateRequestDto request);


  EmployeeStatusUpdateResponseDto updateEmployeeStatus(
      Integer empId,
      EmployeeStatusUpdateRequestDto request
  );
}
