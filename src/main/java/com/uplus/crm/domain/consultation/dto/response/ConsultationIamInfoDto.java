package com.uplus.crm.domain.consultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationIamInfoDto {

    private String title;
    private String content;
    private String aiSummary;
    private String memo;
}