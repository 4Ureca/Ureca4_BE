package com.uplus.crm.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private LocalDateTime expiredAt;
    private String role; // "consultant"(상담사) , "admin"(관리자), "others"(일반) , 슈퍼관리자는 삭제.
}
