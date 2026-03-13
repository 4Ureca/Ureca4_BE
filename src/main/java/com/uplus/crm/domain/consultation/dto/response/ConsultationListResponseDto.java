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
public class ConsultationListResponseDto {

    private List<ConsultationListItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
