package com.uplus.crm.domain.account.dto.response;
// GET /auth/me — 로그인한 계정 정보 조회
// Response

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// Response
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyInfoResponseDto {
    private Integer empId;
    private String loginId;
    private String name;
    private String email;
    private String phone;             // nullable
    private String birth;             // nullable
    private String gender;            // nullable
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Integer deptId;
    private String deptName;
    private Integer jobRoleId;
    private String roleName;
    private String joinedAt;          // nullable
}
