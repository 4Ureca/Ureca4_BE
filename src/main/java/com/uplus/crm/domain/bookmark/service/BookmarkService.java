package com.uplus.crm.domain.bookmark.service;

import java.util.List;

import com.uplus.crm.domain.bookmark.dto.BookmarkToggleResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkResponseDto;

public interface BookmarkService {

    BookmarkToggleResponseDto addManualBookmark(Integer empId, Integer manualId);

    BookmarkToggleResponseDto removeManualBookmark(Integer empId, Integer manualId);

    List<ManualBookmarkResponseDto> getManualBookmarks(Integer empId);

    ManualBookmarkDetailResponseDto getManualBookmarkDetail(Integer empId, Integer manualId);


    BookmarkToggleResponseDto addConsultationBookmark(Integer empId, Long consultId);

    BookmarkToggleResponseDto removeConsultationBookmark(Integer empId, Long consultId);

    List<ConsultationBookmarkResponseDto> getConsultationBookmarks(Integer empId);

    ConsultationBookmarkDetailResponseDto getConsultationBookmarkDetail(Integer empId, Long consultId);
}
