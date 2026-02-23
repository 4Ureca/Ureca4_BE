package com.uplus.crm.domain.account.dto.response;
// GET /admin/employees/{id} — 직원 계정 정보 상세 조회
// Response - 중첩 객체
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
    private String phone;                           // nullable
    private String birth;                           // nullable
    private String gender;                          // nullable
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Integer deptId;
    private String deptName;
    private Integer jobRoleId;
    private String roleName;
    private String joinedAt;                        // nullable
    private List<PermissionDto> deptPermissions;    // 부서 권한 목록
    private List<PermissionDto> empPermissions;     // 개별 권한 목록

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDto {
        private Integer permId;                     // 권한 ID
        private String permCode;                    // 권한 코드
        private String permDesc;                    // 권한 설명 (nullable)
    }
}