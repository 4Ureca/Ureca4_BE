package com.uplus.crm.domain.account.dto.response;
// PUT /admin/employees/{id}/roles — 직원 개별 권한 편집
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// Response - 중첩 객체
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePermissionUpdateResponseDto {
    private Integer empId;                      // 직원 ID
    private String name;                        // 이름
    private List<PermissionDto> permissions;    // 변경된 권한 목록

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDto {
        private Integer permId;                 // 권한 ID
        private String permCode;                // 권한 코드
        private String permDesc;                // 권한 설명 (nullable)
        private LocalDateTime assignedAt;       // 부여일시
    }
}
