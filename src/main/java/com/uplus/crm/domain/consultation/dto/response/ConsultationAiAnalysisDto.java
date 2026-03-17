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
    private String complaintReason;
    private Boolean defenseAttempted;
    private Boolean defenseSuccess;
    private String defenseActions;
    
 // 새롭게 추가된 아웃바운드 관련 필드
    private String outboundCallResult; // outbound_call_result 컬럼 대응
    private String outboundReport;     // outbound_report 컬럼 대응
    private String evaluationReason;   // evaluation_reason 대응
    private String rawSummary;
    
}