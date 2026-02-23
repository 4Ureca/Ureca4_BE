package com.uplus.crm.domain.account.dto.response;
// GET /admin/employees — 직원 계정 정보 목록 조회
// QueryParameter (별도 @RequestParam으로 처리, DTO 불필요)
// - page, size, dept_id, job_role_id, status, keyword
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
public class EmployeeListResponseDto {
    private List<EmployeeDto> content;      // 직원 목록
    private Long totalElements;             // 전체 건수
    private Integer totalPages;             // 전체 페이지 수
    private Integer page;                   // 현재 페이지
    private Integer size;                   // 페이지 크기

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDto {
        private Integer empId;              // 직원 ID
        private String loginId;             // 사번
        private String name;                // 이름
        private String email;               // 이메일
        private String phone;               // 전화번호 (nullable)
        private Boolean isActive;           // 활성화 상태
        private String deptName;            // 부서명
        private String roleName;            // 역할명
        private String joinedAt;            // 입사일 (nullable)
        private LocalDateTime createdAt;    // 생성일
    }
}
