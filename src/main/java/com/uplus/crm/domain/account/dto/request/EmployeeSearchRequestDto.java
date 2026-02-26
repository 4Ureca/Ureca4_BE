package com.uplus.crm.domain.account.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSearchRequestDto {
    private Integer page = 0;
    private Integer size = 20;

    @Parameter(name = "dept_id")
    private Integer deptId;

    @Parameter(name = "job_role_id")
    private Integer jobRoleId;

    private String status; // ACTIVE, INACTIVE
    private String keyword; // 이름 또는 사번
}