package com.uplus.crm.domain.account.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


// POST /admin/employees — 직원 계정 정보 생성
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateResponseDto {
    private Integer empId;            // 생성된 직원 ID
    private String loginId;           // 사번
    private String name;              // 이름
    private String deptName;          // 부서명
    private String roleName;          // 역할명
    private LocalDateTime createdAt;  // 생성일시
}
