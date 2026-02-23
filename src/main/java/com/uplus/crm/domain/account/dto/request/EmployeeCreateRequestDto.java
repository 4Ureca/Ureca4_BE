package com.uplus.crm.domain.account.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// POST /admin/employees — 직원 계정 정보 생성
@Getter
@NoArgsConstructor
public class EmployeeCreateRequestDto {
    private String loginId;           // 사번 (중복불가)
    private String password;          // 초기 비밀번호
    private String name;              // 이름
    private String email;             // 이메일
    private String phone;             // 전화번호 (nullable)
    private String birth;             // 생년월일 (nullable, "1998-05-15")
    private String gender;            // 성별 (nullable, male/female/other)
    private Integer deptId;           // 부서 ID
    private Integer jobRoleId;        // 역할 ID
    private String joinedAt;          // 입사일 (nullable, "2026-02-22")
    private List<Integer> permissionIds; // 개별 권한 ID 목록 (nullable)
}
