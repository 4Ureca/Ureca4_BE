package com.uplus.crm.domain.account.dto.request;
// PUT /admin/employees/{id}/status — 직원 계정 활성화/비활성화
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Request
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatusUpdateRequestDto {
    private Boolean isActive;         // 활성화 상태
}
