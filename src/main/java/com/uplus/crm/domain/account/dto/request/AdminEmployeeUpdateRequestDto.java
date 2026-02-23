package com.uplus.crm.domain.account.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// PUT /admin/employees/{id} — 직원 계정 정보 편집
// Request
@Getter
@NoArgsConstructor
public class AdminEmployeeUpdateRequestDto {
    private String name;              // 이름
    private String email;             // 이메일
    private String phone;             // 전화번호 (nullable)
    private String birth;             // 생년월일 (nullable)
    private String gender;            // 성별 (nullable)
    private Integer deptId;           // 부서 ID
    private Integer jobRoleId;        // 역할 ID
    private String joinedAt;          // 입사일 (nullable)
}
