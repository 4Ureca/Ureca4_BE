package com.uplus.crm.domain.account.dto.response;
//GET /admin/departments — 부서 권한 목록 조회
//Request/QueryParameter 없음.
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
public class DepartmentListResponseDto {
    private List<DepartmentDto> departments;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentDto {
        private Integer deptId;                     // 부서 ID
        private String deptName;                    // 부서명
        private String location;                    // 위치 (nullable)
        private String phoneNumber;                       // 부서 전화번호 (nullable)
        private List<PermissionDto> permissions;    // 부서 권한 목록

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PermissionDto {
            private Integer permId;                 // 권한 ID
            private String permCode;                // 권한 코드
            private String permDesc;                // 권한 설명 (nullable)
            private LocalDateTime assignedAt;       // 부여일
        }
    }
}
