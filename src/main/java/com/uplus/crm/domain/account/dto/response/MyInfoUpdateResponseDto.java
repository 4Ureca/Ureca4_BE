package com.uplus.crm.domain.account.dto.response;
// PUT /auth/me — 내 정보 편집
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyInfoUpdateResponseDto {
    private Integer empId;            // 직원 ID
    private String name;              // 수정된 이름
    private String email;             // 수정된 이메일
    private String phone;             // 수정된 전화번호 (nullable)
    private String birth;             // 수정된 생년월일 (nullable)
    private String gender;            // 수정된 성별 (nullable)
}
