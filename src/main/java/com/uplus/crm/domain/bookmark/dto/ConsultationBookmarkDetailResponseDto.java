package com.uplus.crm.domain.bookmark.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationBookmarkDetailResponseDto {

    private Long bookmarkId;
    private Long consultId;
    private String summary;
    private String result;
    private LocalDateTime consultationCreatedAt;
    private LocalDateTime bookmarkedAt;

}
