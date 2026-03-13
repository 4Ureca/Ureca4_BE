package com.uplus.crm.domain.consultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationListItemDto {

    private Long consultId;
    private String consultationNumber;
    private String customerName;
    private String customerPhone;
    private String consultationType;
    private String channel;
    private String counselorName;
    private String processStatus;
    private String summaryStatus;
    private Integer satisfaction;
    private String consultedAt;
}