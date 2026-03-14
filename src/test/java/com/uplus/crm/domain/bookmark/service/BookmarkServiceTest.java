package com.uplus.crm.domain.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uplus.crm.common.exception.BookmarkException;
import com.uplus.crm.domain.account.repository.mysql.UserBookmarkRepository;
import com.uplus.crm.domain.bookmark.dto.BookmarkToggleResponseDto;
import com.uplus.crm.domain.bookmark.entity.UserBookmark;
import com.uplus.crm.domain.consultation.repository.ConsultationResultRepository;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    @Mock
    private UserBookmarkRepository userBookmarkRepository;

    @Mock
    private ConsultationResultRepository consultationResultRepository;

    private final Integer TEST_EMP_ID = 1;
    private final Integer TEST_MANUAL_ID = 100;
    private final Long TEST_CONSULT_ID = 500L;

    @Nested
    @DisplayName("운영정책(Manual) 북마크 테스트")
    class ManualBookmarkTests {

        @Test
        @DisplayName("매뉴얼 북마크 추가 성공")
        void addManualBookmark_Success() {
            // given
            given(userBookmarkRepository.existsByEmpIdAndManualId(TEST_EMP_ID, TEST_MANUAL_ID)).willReturn(false);

            // when
            BookmarkToggleResponseDto response = bookmarkService.addManualBookmark(TEST_EMP_ID, TEST_MANUAL_ID);

            // then
            assertThat(response.isBookmarked()).isTrue();
            assertThat(response.getType()).isEqualTo("MANUAL");
            verify(userBookmarkRepository, times(1)).save(any(UserBookmark.class));
        }

        @Test
        @DisplayName("이미 등록된 매뉴얼 북마크 추가 시 예외 발생")
        void addManualBookmark_AlreadyExists_ThrowsException() {
            // given
            given(userBookmarkRepository.existsByEmpIdAndManualId(TEST_EMP_ID, TEST_MANUAL_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> bookmarkService.addManualBookmark(TEST_EMP_ID, TEST_MANUAL_ID))
                    .isInstanceOf(BookmarkException.class);
        }

        @Test
        @DisplayName("매뉴얼 북마크 삭제 성공")
        void removeManualBookmark_Success() {
            // given
            UserBookmark bookmark = UserBookmark.builder().empId(TEST_EMP_ID).manualId(TEST_MANUAL_ID).build();
            given(userBookmarkRepository.findByEmpIdAndManualId(TEST_EMP_ID, TEST_MANUAL_ID)).willReturn(Optional.of(bookmark));

            // when
            BookmarkToggleResponseDto response = bookmarkService.removeManualBookmark(TEST_EMP_ID, TEST_MANUAL_ID);

            // then
            assertThat(response.isBookmarked()).isFalse();
            verify(userBookmarkRepository, times(1)).delete(bookmark);
        }
    }

    @Nested
    @DisplayName("상담요약(Consultation) 북마크 테스트")
    class ConsultationBookmarkTests {

        @Test
        @DisplayName("상담요약 북마크 추가 성공")
        void addConsultationBookmark_Success() {
            // given
            given(consultationResultRepository.existsById(TEST_CONSULT_ID)).willReturn(true);
            given(userBookmarkRepository.existsByEmpIdAndConsultId(TEST_EMP_ID, TEST_CONSULT_ID)).willReturn(false);

            // when
            BookmarkToggleResponseDto response = bookmarkService.addConsultationBookmark(TEST_EMP_ID, TEST_CONSULT_ID);

            // then
            assertThat(response.isBookmarked()).isTrue();
            assertThat(response.getType()).isEqualTo("CONSULTATION");
            verify(userBookmarkRepository, times(1)).save(any(UserBookmark.class));
        }

        @Test
        @DisplayName("존재하지 않는 상담ID로 북마크 추가 시 예외 발생")
        void addConsultationBookmark_NotFound_ThrowsException() {
            // given
            given(consultationResultRepository.existsById(TEST_CONSULT_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> bookmarkService.addConsultationBookmark(TEST_EMP_ID, TEST_CONSULT_ID))
                    .isInstanceOf(BookmarkException.class);
        }

        @Test
        @DisplayName("상담요약 북마크 삭제 성공")
        void removeConsultationBookmark_Success() {
            // given
            UserBookmark bookmark = UserBookmark.builder().empId(TEST_EMP_ID).consultId(TEST_CONSULT_ID).build();
            given(userBookmarkRepository.findByEmpIdAndConsultId(TEST_EMP_ID, TEST_CONSULT_ID)).willReturn(Optional.of(bookmark));

            // when
            BookmarkToggleResponseDto response = bookmarkService.removeConsultationBookmark(TEST_EMP_ID, TEST_CONSULT_ID);

            // then
            assertThat(response.isBookmarked()).isFalse();
            verify(userBookmarkRepository, times(1)).delete(bookmark);
        }
    }
}