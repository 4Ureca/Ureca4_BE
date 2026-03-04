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
public class TokenRefreshResponseDto {

    private String accessToken;
    private LocalDateTime expiredAt;
}
