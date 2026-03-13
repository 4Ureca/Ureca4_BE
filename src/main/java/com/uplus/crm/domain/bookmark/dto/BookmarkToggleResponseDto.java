package com.uplus.crm.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkToggleResponseDto {
    private String type;
    private Long targetId;
    private boolean bookmarked;
    private String message;
}