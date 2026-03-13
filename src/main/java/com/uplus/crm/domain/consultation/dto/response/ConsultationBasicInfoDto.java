package com.uplus.crm.domain.consultation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationBasicInfoDto {

    private Long consultId;
    private String consultationNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String channel;
    private String consultationType;
    private String processStatus;
    private String counselorName;
    private Integer satisfaction;
    private String consultedAt;
    private Integer durationMinutes;
    private String relatedProducts;
    private String tags;
}
