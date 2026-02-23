package com.uplus.crm.domain.account.dto.response;
// PUT /admin/employees/{id}/status — 직원 계정 활성화/비활성화
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Response
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatusUpdateResponseDto {
    private Integer empId;            // 직원 ID
    private String name;              // 이름
    private Boolean isActive;         // 변경된 상태
    private String message;           // 결과 메시지
}
