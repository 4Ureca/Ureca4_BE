package com.uplus.crm.domain.consultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationAiAnalysisDto {

    private String categoryCode;
    private String categoryName;
    private Boolean hasIntent;
    private String complaintReason;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private String defenseActions;
    private String rawSummary;
}