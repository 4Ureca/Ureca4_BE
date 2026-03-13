package com.uplus.crm.domain.consultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationHistoryItemDto {

    private String occurredAt;
    private String title;
    private String description;
}