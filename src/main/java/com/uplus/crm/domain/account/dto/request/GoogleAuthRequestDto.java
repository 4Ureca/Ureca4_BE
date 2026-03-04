package com.uplus.crm.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GoogleAuthRequestDto {

    @NotBlank(message = "Authorization code는 필수입니다.")
    private String authorizationCode;

    @NotBlank(message = "Redirect URI는 필수입니다.")
    private String redirectUri;
}
