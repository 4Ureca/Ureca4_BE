package com.uplus.crm.domain.consultation.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDetailResponseDto {

    private ConsultationBasicInfoDto basicInfo;
    private ConsultationIamInfoDto iamInfo;
    private String rawTextJson;
    private ConsultationAiAnalysisDto aiAnalysis;
    private List<ConsultationHistoryItemDto> history;
}
