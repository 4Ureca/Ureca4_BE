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
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.repository.ConsultationResultRepository;
import com.uplus.crm.domain.manual.entity.Manual;
import com.uplus.crm.domain.manual.repository.ManualRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;
    private final ConsultationResultRepository consultationResultRepository;
    private final ManualRepository manualRepository; //

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
        // 1. 해당 사용자가 실제로 이 매뉴얼을 북마크했는지 확인
        UserBookmark bookmark = userBookmarkRepository.findByEmpIdAndManualId(empId, manualId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.MANUAL_BOOKMARK_NOT_FOUND));

        // 2. 실제 매뉴얼 상세 정보 가져오기
        Manual manual = manualRepository.findById(manualId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.MANUAL_BOOKMARK_NOT_FOUND));

        // 3. DTO로 변환해서 리턴 (엔티티에 있는 모든 데이터를 매핑)
        return ManualBookmarkDetailResponseDto.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .manualId(manual.getManualId())
                .title(manual.getTitle())
                .content(manual.getContent())
                .isActive(manual.getIsActive())
                // 카테고리 정보 매핑
                .category(manual.getCategoryPolicy() != null ? 
                         String.format("[%s > %s > %s]", 
                             manual.getCategoryPolicy().getLargeCategory(),
                             manual.getCategoryPolicy().getMediumCategory(),
                             manual.getCategoryPolicy().getSmallCategory()) : null)
                // 작성자 및 상태 정보
                .createdBy(manual.getEmployee() != null ? manual.getEmployee().getEmpId() : null)
                .status(manual.getIsActive() != null && manual.getIsActive() ? "ACTIVE" : "INACTIVE")
                // 시간 정보
                .manualCreatedAt(manual.getCreatedAt())
                .manualUpdatedAt(manual.getUpdatedAt())
                .bookmarkedAt(bookmark.getCreatedAt())
                // 엔티티에 없는 필드들은 기획에 따라 null 유지 혹은 기본값 세팅
                .tags(null) 
                .targetCustomerType(null)
                .relatedManualIds(null)
                .build();
    }

    @Override
    public ConsultationBookmarkDetailResponseDto getConsultationBookmarkDetail(Integer empId, Long consultId) {
        // 1. 해당 사용자가 이 상담 결과를 북마크했는지 확인
        UserBookmark bookmark = userBookmarkRepository.findByEmpIdAndConsultId(empId, consultId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.CONSULTATION_BOOKMARK_NOT_FOUND));

        // 2. 실제 상담 상세 정보 가져오기
        ConsultationResult consultation = consultationResultRepository.findById(consultId)
                .orElseThrow(() -> new BookmarkException(BookmarkErrorCode.CONSULTATION_NOT_FOUND));

        // 3. DTO에 담아서 반환 (엔티티의 iam 필드들을 조합)
        return ConsultationBookmarkDetailResponseDto.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .consultId(consultation.getConsultId())
                // iamIssue를 summary(요약)로 사용
                .summary(consultation.getIamIssue()) 
                // iamAction과 iamMemo를 합쳐서 전체 결과로 보여줌
                .result(String.format("Action: %s / Memo: %s", 
                        consultation.getIamAction(), 
                        consultation.getIamMemo())) 
                .consultationCreatedAt(consultation.getCreatedAt())
                .bookmarkedAt(bookmark.getCreatedAt())
                .build();
    }
}