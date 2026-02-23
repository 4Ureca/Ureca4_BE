package com.uplus.crm.domain.account.dto.request;
// PUT /admin/employees/{id}/roles — 직원 개별 권한 편집

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EmployeePermissionUpdateRequestDto {
    private List<Integer> permissionIds; // 부여할 권한 ID 목록 (전체 교체)
}
