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
public class ManualBookmarkDetailResponseDto {

    private Long bookmarkId;
    private Integer manualId;
    private String title;
    private String content;
    private Boolean isActive;
    private String category;
    private String tags;
    private String targetCustomerType;
    private Integer createdBy;
    private String status;
    private String relatedManualIds;
    private LocalDateTime manualCreatedAt;
    private LocalDateTime manualUpdatedAt;
    private LocalDateTime bookmarkedAt;
}