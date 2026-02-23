package com.uplus.crm.domain.account.dto.response;
// PUT /admin/employees/{id} — 직원 계정 정보 편집
// Response
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEmployeeUpdateResponseDto {
    private Integer empId;            // 직원 ID
    private String name;              // 수정된 이름
    private String email;             // 수정된 이메일
    private String deptName;          // 수정된 부서명
    private String roleName;          // 수정된 역할명
    private LocalDateTime updatedAt;  // 수정일시
}
