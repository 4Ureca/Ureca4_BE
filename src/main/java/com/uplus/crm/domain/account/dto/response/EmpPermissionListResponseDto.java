package com.uplus.crm.domain.account.dto.response;
//GET /admin/permissions — 개별 권한 목록 조회

// QueryParameter (별도 @RequestParam으로 처리, DTO 불필요)
// - emp_id (Integer, 필수)
// - is_deleted (Integer, nullable)
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
public class EmpPermissionListResponseDto {
    private List<EmpPermissionDto> empPermissions;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpPermissionDto {
        private Integer empPermId;              // 매핑 PK ID
        private Integer empId;                  // 직원 ID
        private Integer permId;                 // 권한 마스터 ID
        private String permCode;                // 권한 코드명
        private String permDesc;                // 권한 설명 (nullable)
        private LocalDateTime assignedAt;       // 권한 부여 일시
        private Integer isDeleted;              // 활성화 상태 (0:유효, 1:삭제)
    }
}