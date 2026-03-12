package com.uplus.crm.domain.bookmark.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BookmarkErrorCode;
import com.uplus.crm.common.exception.BookmarkException;
import com.uplus.crm.domain.account.repository.mysql.UserBookmarkRepository;
import com.uplus.crm.domain.bookmark.dto.BookmarkToggleResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ConsultationBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkDetailResponseDto;
import com.uplus.crm.domain.bookmark.dto.ManualBookmarkResponseDto;
import com.uplus.crm.domain.bookmark.entity.UserBookmark;
import com.uplus.crm.domain.consultation.repository.ConsultationResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;
    private final ConsultationResultRepository consultationResultRepository;

    @Override
    @Transactional
    public BookmarkToggleResponseDto addManualBookmark(Integer empId, Integer manualId) {
        if (userBookmarkRepository.existsByEmpIdAndManualId(empId, manualId)) {
            throw new BookmarkException(BookmarkErrorCode.MANUAL_BOOKMARK_ALREADY_EXISTS);
        }

        UserBookmark bookmark = UserBookmark.builder()
                .empId(empId)
                .manualId(manualId)
                .consultId(null)
                .bestPracticeId(null)
                .build();

        userBookmarkRepository.save(bookmark);

        return BookmarkToggleResponseDto.builder()
                .type("MANUAL")
                .targetId(manualId.longValue())
                .bookmarked(true)
                .message("운영정책 북마크 등록 완료")
                .build();
    }

    @Override
    @Transactional
    public BookmarkToggleResponseDto removeManualBookmark(Integer empId, Integer manualId) {
        UserBookmark bookmark = userBookmarkRepository.findByEmpIdAndManualId(empId, manualId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.MANUAL_BOOKMARK_NOT_FOUND));

        userBookmarkRepository.delete(bookmark);

        return BookmarkToggleResponseDto.builder()
                .type("MANUAL")
                .targetId(manualId.longValue())
                .bookmarked(false)
                .message("운영정책 북마크 해제 완료")
                .build();
    }

    @Override
    public List<ManualBookmarkResponseDto> getManualBookmarks(Integer empId) {
        return userBookmarkRepository.findAllByEmpIdAndManualIdIsNotNullOrderByCreatedAtDesc(empId)
                .stream()
                .map(bookmark -> ManualBookmarkResponseDto.builder()
                        .bookmarkId(bookmark.getBookmarkId())
                        .manualId(bookmark.getManualId())
                        .createdAt(bookmark.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public BookmarkToggleResponseDto addConsultationBookmark(Integer empId, Long consultId) {
        if (!consultationResultRepository.existsById(consultId)) {
            throw new BookmarkException(BookmarkErrorCode.CONSULTATION_NOT_FOUND);
        }

        if (userBookmarkRepository.existsByEmpIdAndConsultId(empId, consultId)) {
            throw new BookmarkException(BookmarkErrorCode.CONSULTATION_BOOKMARK_ALREADY_EXISTS);
        }

        UserBookmark bookmark = UserBookmark.builder()
                .empId(empId)
                .manualId(null)
                .consultId(consultId)
                .bestPracticeId(null)
                .build();

        userBookmarkRepository.save(bookmark);

        return BookmarkToggleResponseDto.builder()
                .type("CONSULTATION")
                .targetId(consultId)
                .bookmarked(true)
                .message("상담요약 북마크 등록 완료")
                .build();
    }

    @Override
    @Transactional
    public BookmarkToggleResponseDto removeConsultationBookmark(Integer empId, Long consultId) {
        UserBookmark bookmark = userBookmarkRepository.findByEmpIdAndConsultId(empId, consultId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.CONSULTATION_BOOKMARK_NOT_FOUND));

        userBookmarkRepository.delete(bookmark);

        return BookmarkToggleResponseDto.builder()
                .type("CONSULTATION")
                .targetId(consultId)
                .bookmarked(false)
                .message("상담요약 북마크 해제 완료")
                .build();
    }

    @Override
    public List<ConsultationBookmarkResponseDto> getConsultationBookmarks(Integer empId) {
        return userBookmarkRepository.findAllByEmpIdAndConsultIdIsNotNullOrderByCreatedAtDesc(empId)
                .stream()
                .map(bookmark -> ConsultationBookmarkResponseDto.builder()
                        .bookmarkId(bookmark.getBookmarkId())
                        .consultId(bookmark.getConsultId())
                        .createdAt(bookmark.getCreatedAt())
                        .build())
                .toList();
    }

	@Override
	public ManualBookmarkDetailResponseDto getManualBookmarkDetail(Integer empId, Integer manualId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConsultationBookmarkDetailResponseDto getConsultationBookmarkDetail(Integer empId, Long consultId) {
		// TODO Auto-generated method stub
		return null;
	}
}