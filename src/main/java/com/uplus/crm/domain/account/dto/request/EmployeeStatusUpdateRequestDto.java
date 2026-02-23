package com.uplus.crm.domain.account.dto.request;
// PUT /admin/employees/{id}/status — 직원 계정 활성화/비활성화
import lombok.Getter;
import lombok.NoArgsConstructor;

// Request
@Getter
@NoArgsConstructor
public class EmployeeStatusUpdateRequestDto {
    private Boolean isActive;         // 활성화 상태
}
