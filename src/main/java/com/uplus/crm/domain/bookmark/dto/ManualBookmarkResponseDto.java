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
public class ManualBookmarkResponseDto {
    private Long bookmarkId;
    private Integer manualId;
    private LocalDateTime createdAt;
}