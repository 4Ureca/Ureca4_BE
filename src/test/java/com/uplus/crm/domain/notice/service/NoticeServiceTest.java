package com.uplus.crm.domain.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.request.NoticeUpdateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeListResponse;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.repository.NoticeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("공지 생성 성공")
    void createNotice_success() {
        Employee employee = Employee.builder().empId(1).build();
        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("점검 공지");
        request.setContent("시스템 점검 안내");
        request.setStatus(NoticeStatus.ACTIVE);
        request.setIsPinned(true);
        request.setVisibleFrom(LocalDateTime.of(2026, 2, 22, 9, 0));
        request.setVisibleTo(LocalDateTime.of(2026, 2, 23, 18, 0));

        given(employeeRepository.findById(1)).willReturn(Optional.of(employee));
        given(noticeRepository.save(any(Notice.class))).willAnswer(invocation -> {
            Notice saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "noticeId", 10);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 2, 22, 8, 0));
            return saved;
        });

        NoticeResponse response = noticeService.createNotice(1, request);

        assertThat(response.getNoticeId()).isEqualTo(10);
        assertThat(response.getEmpId()).isEqualTo(1);
        assertThat(response.getTitle()).isEqualTo("점검 공지");
        assertThat(response.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @Test
    @DisplayName("공지 생성 실패 - 직원 정보 없음")
    void createNotice_fail_employeeNotFound() {
        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("공지");
        request.setContent("내용");
        request.setStatus(NoticeStatus.DRAFT);

        given(employeeRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.createNotice(999, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    @DisplayName("공지 생성 실패 - 노출 기간 역전")
    void createNotice_fail_invalidVisiblePeriod() {
        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("공지");
        request.setContent("내용");
        request.setStatus(NoticeStatus.DRAFT);
        request.setVisibleFrom(LocalDateTime.of(2026, 2, 24, 10, 0));
        request.setVisibleTo(LocalDateTime.of(2026, 2, 23, 10, 0));

        assertThatThrownBy(() -> noticeService.createNotice(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));

        then(noticeRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("공지 상세 조회 시 조회수가 1 증가한다")
    void getNotice_increaseViewCount() {
        Notice notice = Notice.builder()
                .title("공지")
                .content("내용")
                .employee(Employee.builder().empId(1).build())
                .isPinned(false)
                .viewCount(5)
                .status(NoticeStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 2, 22, 9, 0))
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", 4);

        given(noticeRepository.findById(4)).willReturn(Optional.of(notice));

        NoticeResponse response = noticeService.getNotice(4);

        assertThat(response.getNoticeId()).isEqualTo(4);
        assertThat(response.getViewCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("공지 삭제는 DELETED 상태로 변경한다")
    void deleteNotice_softDelete() {
        Notice notice = Notice.builder()
                .title("공지")
                .content("내용")
                .employee(Employee.builder().empId(1).build())
                .isPinned(false)
                .viewCount(0)
                .status(NoticeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        given(noticeRepository.findById(1)).willReturn(Optional.of(notice));

        noticeService.deleteNotice(1);

        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.DELETED);
    }

    @Test
    @DisplayName("공지 목록 조회 - 페이징")
    void getNotices_success() {
        Notice first = Notice.builder()
                .title("첫 공지")
                .content("내용1")
                .employee(Employee.builder().empId(1).build())
                .isPinned(true)
                .viewCount(3)
                .status(NoticeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        Notice second = Notice.builder()
                .title("둘 공지")
                .content("내용2")
                .employee(Employee.builder().empId(2).build())
                .isPinned(false)
                .viewCount(2)
                .status(NoticeStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        PageRequest pageRequest = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Order.desc("isPinned"), Sort.Order.desc("createdAt"))
        );
        given(noticeRepository.findAllByStatusNot(NoticeStatus.DELETED, pageRequest))
                .willReturn(new PageImpl<>(List.of(first, second), pageRequest, 12));

        NoticeListResponse response = noticeService.getNotices(0, 10);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("첫 공지");
        assertThat(response.getTotalElements()).isEqualTo(12);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("공지 목록 조회 실패 - 잘못된 페이징 파라미터")
    void getNotices_fail_invalidPageParams() {
        assertThatThrownBy(() -> noticeService.getNotices(-1, 0))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("공지 수정 실패 - 노출 기간 역전")
    void updateNotice_fail_invalidVisiblePeriod() {
        NoticeUpdateRequest request = new NoticeUpdateRequest();
        request.setTitle("수정공지");
        request.setContent("수정내용");
        request.setStatus(NoticeStatus.ACTIVE);
        request.setIsPinned(false);
        request.setVisibleFrom(LocalDateTime.of(2026, 3, 2, 10, 0));
        request.setVisibleTo(LocalDateTime.of(2026, 3, 1, 10, 0));

        assertThatThrownBy(() -> noticeService.updateNotice(1, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));
    }
}
