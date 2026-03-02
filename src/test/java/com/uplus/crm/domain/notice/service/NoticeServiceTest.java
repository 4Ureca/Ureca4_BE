package com.uplus.crm.domain.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import com.uplus.crm.domain.notice.dto.request.NoticeCreateRequest;
import com.uplus.crm.domain.notice.dto.response.NoticeResponse;
import com.uplus.crm.domain.notice.entity.Notice;
import com.uplus.crm.domain.notice.entity.NoticeStatus;
import com.uplus.crm.domain.notice.entity.NoticeType;
import com.uplus.crm.domain.notice.entity.TargetRole;
import com.uplus.crm.domain.notice.repository.NoticeRepository;
import com.uplus.crm.domain.notification.entity.NoticeReadLog;
import com.uplus.crm.domain.notification.repository.NoticeReadLogRepository;
import com.uplus.crm.domain.notification.repository.UserNotificationRepository;
import com.uplus.crm.domain.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock private NoticeRepository noticeRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private NoticeReadLogRepository noticeReadLogRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private NotificationService notificationService;

    // ── 공통 픽스처 ──────────────────────────────────────────────────────────

    private Employee stubEmployee(int empId) {
        return Employee.builder().empId(empId).name("테스트직원").build();
    }

    private Notice stubActiveNotice(int noticeId, Employee employee) {
        Notice notice = Notice.builder()
                .title("공지").content("내용")
                .employee(employee)
                .status(NoticeStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(notice, "noticeId", noticeId);
        return notice;
    }

    // ── createNotice ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createNotice - visibleFrom이 null이면 ACTIVE 상태로 생성된다")
    void createNotice_nullVisibleFrom_statusActive() {
        NoticeCreateRequest request = new NoticeCreateRequest(
                "제목", "내용", NoticeType.GENERAL, TargetRole.ALL, false, false, null, null);

        given(employeeRepository.findById(1)).willReturn(Optional.of(stubEmployee(1)));
        given(noticeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        NoticeResponse response = noticeService.createNotice(request, 1);

        assertThat(response.status()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @Test
    @DisplayName("createNotice - visibleFrom이 미래이면 SCHEDULED 상태로 생성된다")
    void createNotice_futureVisibleFrom_statusScheduled() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        NoticeCreateRequest request = new NoticeCreateRequest(
                "제목", "내용", NoticeType.GENERAL, TargetRole.ALL, false, false, future, null);

        given(employeeRepository.findById(1)).willReturn(Optional.of(stubEmployee(1)));
        given(noticeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        NoticeResponse response = noticeService.createNotice(request, 1);

        assertThat(response.status()).isEqualTo(NoticeStatus.SCHEDULED);
    }

    @Test
    @DisplayName("createNotice - sendNotification=true이면 알림이 발송된다")
    void createNotice_sendNotification_callsService() {
        NoticeCreateRequest request = new NoticeCreateRequest(
                "제목", "내용", NoticeType.URGENT, TargetRole.ALL, false, true, null, null);

        given(employeeRepository.findById(1)).willReturn(Optional.of(stubEmployee(1)));
        given(noticeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        noticeService.createNotice(request, 1);

        then(notificationService).should().sendNoticeAlert(any(Notice.class));
    }

    @Test
    @DisplayName("createNotice - 직원이 없으면 EMPLOYEE_NOT_FOUND 예외")
    void createNotice_employeeNotFound() {
        NoticeCreateRequest request = new NoticeCreateRequest(
                "제목", "내용", null, null, false, false, null, null);

        given(employeeRepository.findById(999)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.createNotice(request, 999))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    @DisplayName("createNotice - visibleFrom이 visibleTo보다 늦으면 INVALID_INPUT 예외")
    void createNotice_invalidVisiblePeriod_throws() {
        NoticeCreateRequest request = new NoticeCreateRequest(
                "제목", "내용", null, null, false, false,
                LocalDateTime.of(2026, 3, 5, 10, 0),
                LocalDateTime.of(2026, 3, 1, 10, 0));

        assertThatThrownBy(() -> noticeService.createNotice(request, 1))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));

        then(noticeRepository).shouldHaveNoInteractions();
    }

    // ── getNoticeDetail ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getNoticeDetail - 처음 조회 시 읽음 로그가 저장된다")
    void getNoticeDetail_firstRead_savesReadLog() {
        Employee employee = stubEmployee(1);
        Notice notice = stubActiveNotice(10, employee);

        given(noticeRepository.findById(10)).willReturn(Optional.of(notice));
        given(noticeReadLogRepository.existsByNoticeIdAndEmpId(10, 2)).willReturn(false);

        noticeService.getNoticeDetail(10, 2);

        then(noticeReadLogRepository).should().save(any(NoticeReadLog.class));
        then(userNotificationRepository).should().markNoticeAlertAsRead(2, 10L);
    }

    @Test
    @DisplayName("getNoticeDetail - 이미 읽은 경우 읽음 로그를 저장하지 않는다")
    void getNoticeDetail_alreadyRead_skipReadLog() {
        Employee employee = stubEmployee(1);
        Notice notice = stubActiveNotice(10, employee);

        given(noticeRepository.findById(10)).willReturn(Optional.of(notice));
        given(noticeReadLogRepository.existsByNoticeIdAndEmpId(10, 2)).willReturn(true);

        noticeService.getNoticeDetail(10, 2);

        then(noticeReadLogRepository).should(never()).save(any());
    }

    // ── getNotice (backward compat) ───────────────────────────────────────────

    @Test
    @DisplayName("getNotice - 조회 시 viewCount가 1 증가한다")
    void getNotice_increaseViewCount() {
        Employee employee = stubEmployee(1);
        Notice notice = stubActiveNotice(4, employee);

        given(noticeRepository.findById(4)).willReturn(Optional.of(notice));

        NoticeResponse response = noticeService.getNotice(4);

        assertThat(response.noticeId()).isEqualTo(4);
        assertThat(response.viewCount()).isEqualTo(1);
    }

    // ── deleteNotice ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteNotice - status가 DELETED로 변경된다")
    void deleteNotice_softDelete() {
        Employee employee = stubEmployee(1);
        Notice notice = stubActiveNotice(1, employee);

        given(noticeRepository.findById(1)).willReturn(Optional.of(notice));

        noticeService.deleteNotice(1, 1);

        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.DELETED);
    }

    // ── getNotices (backward compat) ──────────────────────────────────────────

    @Test
    @DisplayName("getNotices - 잘못된 페이징 파라미터이면 INVALID_INPUT 예외")
    void getNotices_invalidPageParams_throws() {
        assertThatThrownBy(() -> noticeService.getNotices(-1, 0))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT));
    }
}
