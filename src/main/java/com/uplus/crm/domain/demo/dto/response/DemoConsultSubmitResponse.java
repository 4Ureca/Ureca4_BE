package com.uplus.crm.domain.demo.dto.response;

import java.time.LocalDateTime;

public record DemoConsultSubmitResponse(
        Long consultId,
        LocalDateTime createdAt
) {
}
