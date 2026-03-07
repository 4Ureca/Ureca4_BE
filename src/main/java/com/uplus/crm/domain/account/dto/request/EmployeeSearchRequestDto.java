package com.uplus.crm.domain.account.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
public class EmployeeSearchRequestDto {
    private Integer page = 0;
    private Integer size = 20;
    private Integer dept_id;
    private Integer job_role_id;
    @Parameter(description = "계정 상태 (활성화 / 비활성화 / 전체)", example = "활성화")
    private String status;
    private String keyword;
}