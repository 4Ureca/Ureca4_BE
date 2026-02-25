package com.uplus.crm.domain.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpPermissionRequestDto {

    @NotNull
    @Schema(description = "직원 아이디", example = "1")
    private Integer empId;

    @Schema(description = "권한 삭제 유무 필터 (0:유효, 1:삭제)", example = "0")
    private Integer isDeleted;
}