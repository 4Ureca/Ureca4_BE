package com.uplus.crm.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDetailResponseDto {
    private Integer empId;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private String birth;
    private String gender;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Integer deptId;
    private String deptName;
    private Integer jobRoleId;
    private String roleName;
    private String joinedAt;
}